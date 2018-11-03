package ml.dcxo.x.obwei.service

import android.media.MediaPlayer

/**
 * Created by David on 03/11/2018 for ObweiX
 */
interface PlaybackManager {

	val player: MediaPlayer

	fun MediaPlayer.play()
	fun MediaPlayer.pause2()
	fun MediaPlayer.skipToPrevious()
	fun MediaPlayer.skipToNext()

}