package ml.dcxo.x.obwei.ui.dialogs

import android.content.Context
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.createPlaylist
import ml.dcxo.x.obwei.utils.dp

/**
 * Created by David on 12/01/2019 for XOXO
 */
typealias AfterPlaylistCreated = (Int)->Unit

fun createPlaylistDialog(
	context: Context,
	afterPlaylistCreated: AfterPlaylistCreated? = null
): AlertDialog {

	val container = FrameLayout(context)
	val editText = EditText(context)
	editText.layoutParams = FrameLayout.LayoutParams(
		ViewGroup.LayoutParams.MATCH_PARENT,
		ViewGroup.LayoutParams.WRAP_CONTENT
	).apply {
		marginEnd = 8.dp
		marginStart = 8.dp
	}
	container.addView(editText)

	return AlertDialog.Builder(context)
		.setTitle(context.getString(R.string.create_playlist))
		.setView(container)
		.setPositiveButton(R.string.add) { _, _ ->

			if (editText.text.isBlank()) {
				Toast.makeText(context, "Not valid name", Toast.LENGTH_SHORT).show()
				return@setPositiveButton
			}

			val id = createPlaylist(context, editText.text.toString())
			afterPlaylistCreated?.invoke(id)

		}
		.setNegativeButton(R.string.cancel) {dialog, _ -> dialog.dismiss() }
		.create()
}