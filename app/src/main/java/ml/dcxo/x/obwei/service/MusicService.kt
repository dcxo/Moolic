package ml.dcxo.x.obwei.service

import android.app.Service
import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.*
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.LruCache
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist
import java.io.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

/**
 * Created by David on 03/11/2018 for ObweiX
 */
class MusicService: Service(), UIInteractions, PlaybackManager, CoroutineScope {

	lateinit var mSession: MediaSessionCompat
	lateinit var audioManager: AudioManager

	override val coroutineContext: CoroutineContext = Dispatchers.Default
	/* Service state vars */
	val liveServiceState = MutableLiveData<ServiceState>()
	var cSong by Delegates.observable(Song.NULL) {_, _, newSong ->
		liveServiceState.postValue( ServiceState(song = newSong) )
	}
	var cAmbiColor by Delegates.observable(AmbiColor.NULL) {_, _, newAmbiColor ->
		liveServiceState.postValue( ServiceState(ambiColor = newAmbiColor) )
	}
	var cPosition by Delegates.observable(-1) {_, _, newPosition ->
		liveServiceState.postValue( ServiceState(position = newPosition) )
	}
	var cShuffleMode by Delegates.observable(false) {_, _, newShuffleMode ->

		val queue = qQueue
		queue.clear()
		queue.addAll(qSortedQueue)

		cPosition = if (newShuffleMode) {

			queue.remove(cSong)
			queue.shuffle(Random(System.currentTimeMillis()))
			queue.add(0, cSong)
			0

		} else {

			queue.indexOf(cSong)

		}

		liveQueue.postValue(queue)
		liveServiceState.postValue( ServiceState(shuffleMode = newShuffleMode) )
		Settings.get(this).shuffleMode = newShuffleMode
	}
	var cRepeatMode by Delegates.observable( PlaybackStateCompat.REPEAT_MODE_NONE ) { _, _, newRepeatMode ->
		liveServiceState.postValue( ServiceState(repeatMode = newRepeatMode) )
		Settings.get(this).repeatMode = newRepeatMode
	}
	var cPlaybackStateCompat: PlaybackStateCompat by Delegates.observable( PlaybackStateCompat.Builder().build() ) { _, _, newPlaybackState ->
		liveServiceState.postValue( ServiceState(playbackState = newPlaybackState) )
	}
	/* Queue vars */
	val liveQueue = MutableLiveData<Tracklist>()
	val qSortedQueue = arrayListOf<Song>()
	var qQueue by Delegates.observable(arrayListOf<Song>()) {_, _, newQueue ->
		liveQueue.postValue(newQueue)
	}
	/* Current player progress vars */
	val livePlayerProgress = MutableLiveData<Int>()

	private var servicePetitions: ServicePetitions? = null
	private val mBinder = MusicServiceBinder()
	private val preparedListener = MediaPlayer.OnPreparedListener {

		it.play()
		fetchMetadataAndAmbiColor()

	}
	private val noisy = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {

			player.pause()

		}
	}
	private val callback = object : MediaSessionCompat.Callback(){
		override fun onPlay() = player.play()
		override fun onPause() = player.obweiPause()
		override fun onSkipToPrevious() = player.skipToPrevious()
		override fun onSkipToNext() = player.skipToNext(true)
	}
	private val pauseJob = launch(start = CoroutineStart.LAZY) {

		delay( 600_000L )
		stopSelf()

	}
	private val handler = Handler()
	private val progressJob = object: Runnable {
		override fun run() {
			livePlayerProgress.postValue( player.currentPosition )
			handler.postDelayed(this, 100)
		}
	}
	private val ambiColorCache = LruCache<Int, AmbiColor>(Runtime.getRuntime().freeMemory().toInt()/1024/8)
	private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener {

		when (it) {

			AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
				player.play()
				player.setVolume(1f, 1f)
			}
			AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.obweiPause()
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.setVolume(0.15f, 0.15f)

		}

	}
	private val audioAttributes = AudioAttributes.Builder()
		.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
		.setUsage(AudioAttributes.USAGE_MEDIA)
		.build()

	private val audioFocusRequest = if (SDK_INT >= Build.VERSION_CODES.O)
		AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
			.setOnAudioFocusChangeListener(audioFocusChangeListener)
			.setAudioAttributes(audioAttributes)
			.build()
	else null

	override val player: MediaPlayer = MediaPlayer()

	private fun fetchMetadataAndAmbiColor() = launch {

		val mediaMetadataCompat = MediaMetadataCompat.Builder()
			.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, cSong.id.toString())
			.putString(MediaMetadataCompat.METADATA_KEY_TITLE, cSong.title)
			.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cSong.albumTitle)
			.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, cSong.getAlbumArtURI)
			.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cSong.artistName)
			.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, cSong.duration)

		withContext(Dispatchers.IO) {

			var iStream: InputStream? = null

			try {

				iStream = contentResolver.openInputStream(cSong.getAlbumArtURI.toUri())
				val b = BitmapFactory.decodeStream(iStream)

				mediaMetadataCompat.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, b)


			} catch (e: FileNotFoundException) {}
			iStream?.close()

			mSession.setMetadata(mediaMetadataCompat.build())

		}

	}
	private fun setAmbiColor() {
		if (ambiColorCache.get(cSong.albumId) == null) {
			val target = object : SimpleTarget<AmbiBitmap>() {
				override fun onResourceReady(resource: AmbiBitmap?, glideAnimation: GlideAnimation<in AmbiBitmap>?) {

					cAmbiColor = resource?.ambiColor ?: AmbiColor.NULL
					ambiColorCache.put(cSong.albumId, cAmbiColor)

				}

				override fun onLoadFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {
					super.onLoadFailed(e, errorDrawable)

					cAmbiColor = AmbiColor.NULL

				}
			}
			Glide.with(this).load(cSong.getAlbumArtURI).asBitmap()
				.transcode(TranscoderAmBitmap(applicationContext), AmbiBitmap::class.java)
				.error(ColorDrawable(Color.RED)).override(155.dp, 155.dp).into(target)
		} else {
			cAmbiColor = ambiColorCache.get(cSong.albumId)!!
		}
	}
	private fun getAudioFocus(): Int {

		return if (SDK_INT >= Build.VERSION_CODES.O)
			audioManager.requestAudioFocus(audioFocusRequest)
		else
			audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

	}
	private fun changePlaybackState(newState: Int) {

		val progress = livePlayerProgress.value?.toLong() ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
		val rate = if (newState == PlaybackStateCompat.STATE_PAUSED) 0f else 1f
		val actions = PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
				PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
				PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

		cPlaybackStateCompat = PlaybackStateCompat.Builder()
			.setActions(actions)
			.setState(newState, progress, rate)
			.build()

	}

	override fun onCreate() {

		player.setOnCompletionListener { it.skipToNext(false) }
		player.setOnPreparedListener(preparedListener)
		player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)

		registerReceiver(noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

		val componentName = ComponentName(this, MediaButtonReceiver::class.java)

		cShuffleMode = Settings.get(this).shuffleMode
		cRepeatMode = Settings.get(this).repeatMode

		audioManager = getSystemService<AudioManager>() ?: throw IllegalStateException()

		mSession = MediaSessionCompat(this, mediaSessionKey, componentName, null)
		mSession.setCallback(callback)
		mSession.isActive = true

		changePlaybackState( PlaybackStateCompat.STATE_NONE )

	}
	override fun onDestroy() {

		player.stop()
		mSession.setCallback(null)
		mSession.isActive = false
		pauseJob.cancel()
		handler.removeCallbacks(progressJob)
		unregisterReceiver(noisy)

	}
	override fun onBind(intent: Intent?): IBinder? = mBinder

	override fun onSongSelected(song: Song, sortedList: Tracklist, indexInList: Int) {

		cSong = song

		qSortedQueue.clear()
		qSortedQueue.addAll(sortedList)

		qQueue = if (cShuffleMode) {
			sortedList.removeAt(indexInList)
			sortedList.shuffle(Random(System.nanoTime()))
			sortedList.add(0, song)
			cPosition = 0
			sortedList
		} else {
			cPosition = indexInList
			sortedList
		}


		player.obweiPrepare()
		servicePetitions?.openPlayer()

	}
	override fun onPlayPauseButtonClicked() {

		when (cPlaybackStateCompat.state) {
			PlaybackStateCompat.STATE_PLAYING -> player.obweiPause()
			PlaybackStateCompat.STATE_PAUSED  -> player.play()
			else -> throw IllegalStateException()
		}

	}
	override fun onShuffleButtonClicked() { cShuffleMode = !cShuffleMode }
	override fun onRepeatButtonClicked() {

		cRepeatMode = when (cRepeatMode) {
			PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ALL
			PlaybackStateCompat.REPEAT_MODE_ALL  -> PlaybackStateCompat.REPEAT_MODE_ONE
			PlaybackStateCompat.REPEAT_MODE_ONE  -> PlaybackStateCompat.REPEAT_MODE_NONE
			else -> PlaybackStateCompat.REPEAT_MODE_NONE
		}

	}
	override fun onIndexSelected(selectedIndex: Int) {

		cPosition = selectedIndex
		val song = qQueue[cPosition]

		if ( song.id != cSong.id ) {
			cSong = song
			player.obweiPrepare()
		}

	}
	override fun onAddToQueue(song: Song) {

		qQueue.add(cPosition+1, song)
		liveQueue.postValue(qQueue)
		// TODO( Complete AddToQueue() )

	}
	override fun onSeekBarProgressChange(int: Int) {
		player.seekTo(int)
	}

	override fun MediaPlayer.play() {

		handler.post(progressJob)
		pauseJob.cancel()
		start()
		changePlaybackState( PlaybackStateCompat.STATE_PLAYING )

	}
	override fun MediaPlayer.obweiPause() {

		launch { pauseJob.join() }
		pause()
		changePlaybackState( PlaybackStateCompat.STATE_PAUSED )

	}
	override fun MediaPlayer.skipToNext(force: Boolean) {

		var tempPosition = cPosition + 1
		val size = qQueue.size

		when (cRepeatMode) {

			PlaybackStateCompat.REPEAT_MODE_NONE -> if (tempPosition == size) stopSelf() // TODO(REMOVE STOPSELF())
			PlaybackStateCompat.REPEAT_MODE_ALL  -> if (tempPosition == size) tempPosition = 0
			PlaybackStateCompat.REPEAT_MODE_ONE  -> if (!force) tempPosition = cPosition

		}

		cPosition = tempPosition
		cSong = qQueue[cPosition]
		player.obweiPrepare()

	}
	override fun MediaPlayer.skipToPrevious() {

	}
	override fun MediaPlayer.obweiPrepare() {

		setAmbiColor()

		reset()
		try {
			setDataSource(cSong.filePath)
			prepareAsync()
		} catch (e: IOException) {
			skipToNext(true)
		}

	}

	inner class MusicServiceBinder: Binder() {
		fun setServicePetitions(servicePetitions: ServicePetitions) {
			this@MusicService.servicePetitions = servicePetitions
		}

		val uiInteractions; get() = this@MusicService
		val liveData: Triple<LiveData<ServiceState>, LiveData<Tracklist>, LiveData<Int>>
			get() = Triple(
				this@MusicService.liveServiceState,
				this@MusicService.liveQueue,
				this@MusicService.livePlayerProgress
			)
	}
	inner class ServiceState(
		var song: Song = this@MusicService.cSong,
		var ambiColor: AmbiColor = this@MusicService.cAmbiColor,
		var position: Int = this@MusicService.cPosition,
		var shuffleMode: Boolean = this@MusicService.cShuffleMode,
		var repeatMode: Int = this@MusicService.cRepeatMode,
		var playbackState: PlaybackStateCompat = this@MusicService.cPlaybackStateCompat
	)

}