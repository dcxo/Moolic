package ml.dcxo.x.obwei.service

import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 03/11/2018 for ObweiX
 */

interface UIInteractions {

	fun onSongSelected(song: Song, sortedList: Tracklist, indexInList: Int)
	fun onPlayPauseButtonClicked()
	fun onShuffleButtonClicked()
	fun onRepeatButtonClicked()
	fun onIndexSelected(selectedIndex: Int)
	fun onAddToQueue(song: Song)
	fun onSeekBarProgressChange(int: Int)

}