package ml.dcxo.x.obwei.service

import android.media.MediaPlayer

/**
 * Created by David on 03/11/2018 for XOXO
 */
interface PlaybackManager {

	val player: MediaPlayer

	fun MediaPlayer.obweiPrepare()
	fun MediaPlayer.play()
	fun MediaPlayer.obweiPause()
	fun MediaPlayer.skipToPrevious(force: Boolean)
	fun MediaPlayer.skipToNext(force: Boolean)

}