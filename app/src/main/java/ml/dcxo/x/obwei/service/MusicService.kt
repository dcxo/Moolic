package ml.dcxo.x.obwei.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media.session.MediaButtonReceiver
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.mediaSessionKey
import ml.dcxo.x.obwei.viewModel.Song
import kotlin.properties.Delegates

/**
 * Created by David on 03/11/2018 for ObweiX
 */
class MusicService: Service(), UIInteractions, PlaybackManager {

	lateinit var mSession: MediaSessionCompat

	/* Service state vars */
	val liveServiceState = MutableLiveData<ServiceState>()
	var cSong: Song by Delegates.observable(Song()) {_, _, newSong ->
		liveServiceState.postValue( ServiceState(cSong = newSong) )
	}
	var cAmbiColor by Delegates.observable(AmbiColor()) {_, _, newAmbiColor ->
		liveServiceState.postValue( ServiceState(cAmbiColor = newAmbiColor) )
	}
	var cPosition by Delegates.observable(-1) {_, _, newPosition ->
		liveServiceState.postValue( ServiceState(cPosition = newPosition) )
	}
	var cShuffleMode by Delegates.observable(false) {_, _, newShuffleMode ->
		liveServiceState.postValue( ServiceState(cShuffleMode = newShuffleMode) )
	}
	var cRepeatMode by Delegates.observable( PlaybackStateCompat.REPEAT_MODE_NONE ) { _, _, newRepeatMode ->
		liveServiceState.postValue( ServiceState(cRepeatMode = newRepeatMode) )
	}
	/* Queue vars */
	val liveQueue = MutableLiveData<ArrayList<Song>>()
	/* Current player progress vars */
	val liveCurrentPlayerProgress = MutableLiveData<Int>()

	private val mBinder = MusicServiceBinder()
	private val callback = object : MediaSessionCompat.Callback(){
		override fun onPlay() = player.play()
		override fun onPause() = player.pause2()
		override fun onSkipToPrevious() = player.skipToPrevious()
		override fun onSkipToNext() = player.skipToNext()
	}
	private val pauseJob = GlobalScope.launch(start = CoroutineStart.LAZY) {

		delay( 600_000L )
		stopSelf()

	}

	override val player: MediaPlayer = MediaPlayer()

	override fun onCreate() {

		val componentName = ComponentName(this, MediaButtonReceiver::class.java)

		mSession = MediaSessionCompat(this, mediaSessionKey, componentName, null)
		mSession.setCallback(callback)
		mSession.isActive = true

	}
	override fun onBind(intent: Intent?): IBinder? = mBinder

	override fun onSongSelected(song: Song, sortedList: ArrayList<Song>, indexInList: Int) {
		TODO("onSongSelected: To be implemented")
	}
	override fun onPlayPauseButtonClicked() {
		TODO("onPlayPauseButtonClicked: To be implemented")
	}
	override fun onShuffleButtonClicked() {
		TODO("onShuffleButtonClicked: To be implemented")
	}
	override fun onRepeatButtonClicked() {
		TODO("onRepeatButtonClicked: To be implemented")
	}
	override fun onIndexSelected(selectedIndex: Int) {
		TODO("onIndexSelected: To be implemented")
	}
	override fun onAddToQueue(song: Song) {
		TODO("onAddToQueue: To be implemented")
	}

	override fun MediaPlayer.play() {

		pauseJob.cancel()

	}
	override fun MediaPlayer.pause2() {

		GlobalScope.launch { pauseJob.join() }

		pause()

	}
	override fun MediaPlayer.skipToNext() {


	}
	override fun MediaPlayer.skipToPrevious() {

	}

	inner class MusicServiceBinder: Binder() {
		val uiInteractions; get() = this@MusicService
		val liveData: Triple<LiveData<ServiceState>, LiveData<ArrayList<Song>>, LiveData<Int>>
			get() = Triple(
				this@MusicService.liveServiceState,
				this@MusicService.liveQueue,
				this@MusicService.liveCurrentPlayerProgress
			)
	}
	inner class ServiceState(
		var cSong: Song = this@MusicService.cSong,
		var cAmbiColor: AmbiColor = this@MusicService.cAmbiColor,
		var cPosition: Int = this@MusicService.cPosition,
		var cShuffleMode: Boolean = this@MusicService.cShuffleMode,
		var cRepeatMode: Int = this@MusicService.cRepeatMode
	)

}