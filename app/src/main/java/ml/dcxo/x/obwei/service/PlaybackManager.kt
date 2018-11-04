package ml.dcxo.x.obwei.service

import android.media.MediaPlayer

/**
 * Created by David on 03/11/2018 for ObweiX
 */
interface PlaybackManager {

	val player: MediaPlayer

	fun MediaPlayer.obweiPrepare()
	fun MediaPlayer.play()
	fun MediaPlayer.obweiPause()
	fun MediaPlayer.skipToPrevious()
	fun MediaPlayer.skipToNext()

}