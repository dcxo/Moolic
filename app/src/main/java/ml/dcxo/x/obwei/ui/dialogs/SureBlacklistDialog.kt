package ml.dcxo.x.obwei.ui.dialogs

import android.content.Context
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.Settings

/**
 * Created by David on 10/01/2019 for XOXO
 */
fun sureBlacklistDialog(context: Context, blacklistCopy: Array<String>, i: Int, update: (() -> Unit)? = null): AlertDialog {
	return AlertDialog.Builder(context)
		.setTitle(context.getString(R.string.sure_to_remove_to_blacklist))
		.setPositiveButton(R.string.yes) { dialog, _ ->

			val s2 = ArrayList(blacklistCopy.toMutableSet())
			s2.removeAt(i)
			Settings[context].blacklist = s2.toSet()

			context.contentResolver?.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
			update?.invoke()

			dialog.cancel()

		}
		.setNegativeButton(R.string.no) { _, _ ->  }
		.create()
}