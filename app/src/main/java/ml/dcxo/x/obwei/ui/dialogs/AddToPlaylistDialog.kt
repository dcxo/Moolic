package ml.dcxo.x.obwei.ui.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.addToPlaylist
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 13/01/2019 for XOXO
 */
fun addToPlaylistDialog(context: Context, playlists: ArrayList<Playlist>, toAdd: ArrayList<Song>): AlertDialog {

	val array = ArrayList(playlists).apply {
		add(0, Playlist(
			id = -1,
			name = context.getString(R.string.new_playlist)
		))

	}.map { it.name }.toTypedArray()
	return AlertDialog.Builder(context)
		.setItems(array) { _, which ->
			if (which == 0)
				createPlaylistDialog(context) { addToPlaylist(context, toAdd, it) }.show()
			else
				addToPlaylist(context, toAdd, playlists[which-1].id)
		}
		.create()

}