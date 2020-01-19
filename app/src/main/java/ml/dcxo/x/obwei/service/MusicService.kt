package ml.dcxo.x.obwei.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.graphics.BitmapFactory
import android.media.*
import android.os.*
import android.os.Build.VERSION.SDK_INT
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.State
import android.util.LruCache
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media.session.MediaButtonReceiver
import androidx.room.Room
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.db.queue.Queue
import ml.dcxo.x.obwei.db.queue.QueueDB
import ml.dcxo.x.obwei.utils.Settings
import ml.dcxo.x.obwei.utils.mediaSessionKey
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist
import java.io.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

/**
 * Created by David on 03/11/2018 for XOXO
 */
class MusicService: Service(), UIInteractions, PlaybackManager, CoroutineScope {

	lateinit var mSession: MediaSessionCompat
	private lateinit var audioManager: AudioManager
	private lateinit var notification: MusicNotification

	override val coroutineContext: CoroutineContext = Dispatchers.Default
	override val player: MediaPlayer = MediaPlayer()
	/* Service state vars */
	val liveServiceState = MutableLiveData<ServiceState>()
	var cSong by Delegates.observable(Song.NULL) {_, _, newSong ->
		if (this::notification.isInitialized) notification.update()
		liveServiceState.postValue( ServiceState(song = newSong) )
	}
	var cPosition by Delegates.observable(-1) {_, _, newPosition ->
		liveServiceState.postValue( ServiceState(position = newPosition) )
		launch { db.queueDAO().updateQueuePosition(newPosition) }
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

		qQueue = queue
		liveServiceState.postValue( ServiceState(shuffleMode = newShuffleMode) )
		Settings[this].shuffleMode = newShuffleMode
	}
	var cRepeatMode by Delegates.observable( PlaybackStateCompat.REPEAT_MODE_NONE ) { _, _, newRepeatMode ->
		liveServiceState.postValue( ServiceState(repeatMode = newRepeatMode) )
		Settings[this].repeatMode = newRepeatMode
	}
	var cPlaybackStateCompat by Delegates.observable( PlaybackStateCompat.Builder().build() ) { _, _, newPlaybackState ->
		mSession.setPlaybackState(newPlaybackState)
		if (this::notification.isInitialized) notification.update()
		liveServiceState.postValue( ServiceState(playbackState = newPlaybackState) )
	}
	/* Queue vars */
	val liveQueue = MutableLiveData<Tracklist>()
	var qSortedQueue = arrayListOf<Song>()
	var qQueue by Delegates.observable(arrayListOf<Song>()) {_, _, newQueue ->
		saveQueue()
		liveQueue.postValue(newQueue)
	}
	/* Current player progress vars */
	val livePlayerProgress = MutableLiveData<Int>()

	private var servicePetitions: ServicePetitions? = null
	private var isPausedFromAudioFocusChanges = false
	private var isQueueInitializedFromCreate = false

	private val mBinder = MusicServiceBinder()
	private val db by lazy {
		Room
			.databaseBuilder(applicationContext, QueueDB::class.java, "queue-db2")
			.build()
	}
	private val preparedListener = MediaPlayer.OnPreparedListener {

		if (cPlaybackStateCompat.state == PlaybackStateCompat.STATE_PLAYING) it.play()
		fetchMetadataAndAmbiColor()

	}
	private val noisy = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {

			player.obweiPause()

		}
	}
	private val callback = object : MediaSessionCompat.Callback(){
		override fun onPlay() = player.play()
		override fun onPause() = player.obweiPause()
		override fun onSkipToPrevious() = player.skipToPrevious(true)
		override fun onSkipToNext() = player.skipToNext(true)

	}
	private val handler = Handler()
	private val mActions = {
		PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE or
				PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
				PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
	}()
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
				if (isPausedFromAudioFocusChanges) {
					player.play()
					isPausedFromAudioFocusChanges = false
				}
				player.setVolume(1f, 1f)
			}
			AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
				isPausedFromAudioFocusChanges = player.isPlaying
				player.obweiPause()
			}
			AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.setVolume(0.15f, 0.15f)

		}

	}
	private val audioAttributes = {
		AudioAttributes.Builder()
			.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
			.setUsage(AudioAttributes.USAGE_MEDIA)
			.build()
	}()
	private val audioFocusRequest = if (SDK_INT >= Build.VERSION_CODES.O) {
		AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
			.setOnAudioFocusChangeListener(audioFocusChangeListener)
			.setAudioAttributes(audioAttributes)
			.build()
	} else null

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

			val b = try {

				iStream = contentResolver.openInputStream(cSong.getAlbumArtURI.toUri())
				val b = BitmapFactory.decodeStream(iStream)

				b

			} catch (e: FileNotFoundException) {
				null
			}
			iStream?.close()

			notification.bitmap = b
			notification.update()
			mediaMetadataCompat.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, b)

			mSession.setMetadata(mediaMetadataCompat.build())

		}

	}
	private fun getAudioFocus(): Int {

		return if (SDK_INT >= Build.VERSION_CODES.O)
			audioManager.requestAudioFocus(audioFocusRequest)
		else
			audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

	}
	private fun playbackState(@State state: Int, position: Long = -1L): PlaybackStateCompat {
		return PlaybackStateCompat.Builder()
			.setState(state, position, 1f)
			.setActions(mActions).build()
	}
	private fun playPause() {
		when (cPlaybackStateCompat.state) {
			PlaybackStateCompat.STATE_PLAYING -> player.obweiPause()
			PlaybackStateCompat.STATE_PAUSED  -> player.play()
		}
	}
	private fun initQueue(queue: Queue) {

		if (queue.queue.isNotEmpty() || (queue.queue.size == 1 && queue.queue[0] == Song.NULL)) {
			qQueue = queue.queue
			qSortedQueue = queue.sortedQueue
			cPosition = queue.queuePosition

			cSong =
				try { qQueue[if (cPosition >= 0) cPosition else 0] }
				catch (e: IndexOutOfBoundsException) { Song.NULL }

			player.reset()
			try {
				player.setDataSource(cSong.filePath)
				cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_PAUSED)
				player.prepareAsync()
			} catch (e: IOException) {
			}
			isQueueInitializedFromCreate = true
		}

	}
	private fun saveQueue() {

		val queue = Queue(
			queue = qQueue,
			sortedQueue = qSortedQueue,
			queuePosition = cPosition,
			songDurationPosition = 0
		)

		launch { db.queueDAO().insertQueue(queue) }

	}

	override fun onCreate() {

		player.setOnCompletionListener { it.skipToNext(false) }
		player.setOnPreparedListener(preparedListener)
		player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)

		registerReceiver(noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

		cShuffleMode = Settings[this].shuffleMode
		cRepeatMode = Settings[this].repeatMode

		audioManager = getSystemService<AudioManager>() ?: throw IllegalStateException()

		val componentName = ComponentName(this, MediaButtonReceiver::class.java)
		val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply { component = componentName }
		val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

		mSession = MediaSessionCompat(this, mediaSessionKey, componentName, pendingIntent)
		mSession.setCallback(callback)
		mSession.setFlags( MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS )
		mSession.setMediaButtonReceiver(pendingIntent)
		mSession.setMetadata(null)
		cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_NONE)

		notification = MusicNotification()
		notification.init(this)

		mSession.isActive = true

		launch { db.queueDAO().getQueue()?.let { initQueue(it) } }

	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		val what = intent?.action

		when (what) {
			PLAY -> playPause()
			NEXT -> player.skipToNext(true)
			PREVIOUS -> player.skipToPrevious(true)
			DELETE -> stopSelf()
			PLAY_SONG -> {
				val song = intent.getParcelableExtra<Song>(SONG)
				onSongSelected(song, arrayListOf(song), 0)
			}
		}

		return super.onStartCommand(intent, flags, startId)
	}
	override fun onBind(intent: Intent?): IBinder? = mBinder
	override fun onUnbind(intent: Intent?): Boolean {
		servicePetitions = null
		return super.onUnbind(intent)
	}
	override fun onDestroy() {

		db.close()

		player.stop()
		player.release()

		if (SDK_INT >= Build.VERSION_CODES.O)
			audioManager.abandonAudioFocusRequest(audioFocusRequest)
		else
			audioManager.abandonAudioFocus(audioFocusChangeListener)

		mSession.setCallback(null)
		mSession.isActive = false
		mSession.release()

		notification.stop()

		ambiColorCache.evictAll()

		handler.removeCallbacks(progressJob)

		unregisterReceiver(noisy)

	}

	override fun onSongSelected(song: Song, sortedList: Tracklist, indexInList: Int) {

		cSong = song
		val sList = Tracklist(sortedList)

		qSortedQueue.clear()
		qSortedQueue.addAll(sList)

		qQueue = if (cShuffleMode) {
			sList.removeAt(indexInList)
			sList.shuffle(Random(System.nanoTime()))
			sList.add(0, cSong)
			cPosition = 0
			sList
		} else {
			cPosition = indexInList
			sList
		}


		player.obweiPrepare()
		servicePetitions?.openPlayer()

	}
	override fun onShuffleAllButtonClicked(sortedList: Tracklist) {

		qSortedQueue.clear()
		qSortedQueue.addAll(sortedList)

		val l = Tracklist(sortedList)
		l.shuffle(Random(System.currentTimeMillis()))
		qQueue = l

		cSong = l[0]
		cPosition = 0

		player.obweiPrepare()
		servicePetitions?.openPlayer()

	}
	override fun onPlayPauseButtonClicked() {

		playPause()

	}
	override fun onShuffleButtonClicked() { cShuffleMode = !cShuffleMode }
	override fun onShuffleToggle(boolean: Boolean) { cShuffleMode = boolean }
	override fun onRepeatButtonClicked() {

		cRepeatMode = when (cRepeatMode) {
			PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ALL
			PlaybackStateCompat.REPEAT_MODE_ALL  -> PlaybackStateCompat.REPEAT_MODE_ONE
			PlaybackStateCompat.REPEAT_MODE_ONE  -> PlaybackStateCompat.REPEAT_MODE_NONE
			else -> PlaybackStateCompat.REPEAT_MODE_NONE
		}

	}
	override fun onIndexSelected(selectedIndex: Int): Boolean {

		cPosition = selectedIndex
		val song = qQueue[cPosition]

		if ( song.id != cSong.id ) cSong = song
		return player.obweiPrepare()

	}
	override fun onAddToQueue(songs: Tracklist, allSongs: Tracklist, indexInList: Int) {

		if (songs.size == 1) {
			if (cPlaybackStateCompat.state == PlaybackStateCompat.STATE_NONE) onSongSelected(songs[0], allSongs, indexInList)
			else qQueue = qQueue.apply { add(cPosition + 1, songs[0]) }
		} else {
			if (cPlaybackStateCompat.state == PlaybackStateCompat.STATE_NONE) onSongSelected(songs[0], songs, 0)
			else qQueue = qQueue.apply {
				songs.forEachIndexed { index, song ->  add(cPosition + index + 1, song) }
			}
		}

	}
	override fun onSeekBarProgressChange(int: Int) {
		player.seekTo(int)
	}
	override fun onRemoveFromQueue(position: Int) {

		if (position == cPosition) {
			player.skipToNext(true)
			cPosition--
			if (cPosition == -1) cPosition = 0
		}
		try {
			qQueue.removeAt(position)
			qQueue = qQueue
		} catch (e: IndexOutOfBoundsException) {}

	}
	override fun onRemoveQueue() {
		player.reset()
		cPosition = -1
		cSong = Song.NULL
		qQueue = arrayListOf()
		qSortedQueue = arrayListOf()
		cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_NONE)
		notification.stop()
		servicePetitions?.closePlayer(true)
	}
	override fun skipToNext() { player.skipToNext(true) }
	override fun skipToPrevious() { player.skipToPrevious(true) }
	override fun onQueueChange(newQueue: Tracklist) {
		cPosition = newQueue.indexOf(cSong)
		qQueue = newQueue
	}

	override fun MediaPlayer.play() {

		if (getAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			handler.post(progressJob)
			start()
			cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_PLAYING, player.currentPosition.toLong())
		}

	}
	override fun MediaPlayer.obweiPause() {

		pause()
		cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_PAUSED, player.currentPosition.toLong())

	}
	override fun MediaPlayer.skipToNext(force: Boolean) {

		var tempPosition = cPosition + 1
		val size = qQueue.size

		when (cRepeatMode) {

			PlaybackStateCompat.REPEAT_MODE_NONE -> if (tempPosition == size) stopSelf() // TODO(REMOVE STOPSELF())
			PlaybackStateCompat.REPEAT_MODE_ALL  -> if (tempPosition == size) tempPosition = 0
			PlaybackStateCompat.REPEAT_MODE_ONE  -> if (!force) tempPosition = cPosition

		}

		try {
			cPosition = tempPosition
			cSong = qQueue[cPosition]
			player.obweiPrepare()
		} catch (e: IndexOutOfBoundsException) {}

	}
	override fun MediaPlayer.skipToPrevious(force: Boolean) {

		var tempPosition = cPosition - 1
		val size = qQueue.size

		when (cRepeatMode) {

			PlaybackStateCompat.REPEAT_MODE_NONE -> if (tempPosition == -1) stopSelf() // TODO(REMOVE STOPSELF())
			PlaybackStateCompat.REPEAT_MODE_ALL  -> if (tempPosition == -1) tempPosition = size-1
			PlaybackStateCompat.REPEAT_MODE_ONE  -> if (!force) tempPosition = cPosition

		}

		cPosition = tempPosition
		cSong = qQueue[cPosition]
		player.obweiPrepare()
	}
	override fun MediaPlayer.obweiPrepare(): Boolean {

		if (cSong == Song.NULL) return false

		reset()
		return try {
			cPlaybackStateCompat = playbackState(PlaybackStateCompat.STATE_PLAYING)
			setDataSource(cSong.filePath)
			prepareAsync()
			true
		} catch (e: IOException) {
			false
		}

	}

	inner class MusicServiceBinder: Binder() {
		fun setServicePetitions(servicePetitions: ServicePetitions) {
			this@MusicService.servicePetitions = servicePetitions
		}

		fun getAudioSessionId(): Int = player.audioSessionId

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
		var position: Int = this@MusicService.cPosition,
		var shuffleMode: Boolean = this@MusicService.cShuffleMode,
		var repeatMode: Int = this@MusicService.cRepeatMode,
		var playbackState: PlaybackStateCompat = this@MusicService.cPlaybackStateCompat
	)

	companion object {

		const val PLAY = "X_PLAY_X"
		const val PREVIOUS = "X_PREVIOUS_X"
		const val NEXT = "X_NEXT_X"
		const val DELETE = "X_DELETE_X"
		const val PLAY_SONG = "X_PLAY_SONG_x"
		const val SONG = "X_SONG_x"

	}

}