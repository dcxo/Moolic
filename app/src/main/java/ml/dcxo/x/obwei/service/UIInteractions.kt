package ml.dcxo.x.obwei.service

import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 03/11/2018 for XOXO
 */

interface UIInteractions {

	fun onSongSelected(song: Song, sortedList: Tracklist, indexInList: Int)
	fun onPlayPauseButtonClicked()
	fun onShuffleButtonClicked()
	fun onShuffleToggle(boolean: Boolean)
	fun onRepeatButtonClicked()
	fun onIndexSelected(selectedIndex: Int): Boolean
	fun onAddToQueue(songs: Tracklist, allSongs: Tracklist = Tracklist(), indexInList: Int)
	fun onSeekBarProgressChange(int: Int)
	fun onRemoveFromQueue(position: Int)
	fun skipToNext()
	fun skipToPrevious()
	fun onShuffleAllButtonClicked(sortedList: Tracklist)
	fun onRemoveQueue()
	fun onQueueChange(newQueue: Tracklist)

}