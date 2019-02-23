package ml.dcxo.x.obwei.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.utils.addToBlacklist
import java.io.File

/**
 * Created by David on 04/12/2018 for XOXO
 */
class AddToBlacklistDialog: DialogFragment() {

	lateinit var notifyRemove: NotifyRemoveFun

	private var listPos = -1
	private var args = listOf<String>()

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

		val alertDialog = AlertDialog.Builder(activity!!)
			.setTitle(R.string.add_to_blacklist)
			.setNegativeButton(R.string.cancel) { _, _ -> }
			.setPositiveButton(R.string.accept) { _, _ ->
				addToBlacklist(activity, args)
				activity?.contentResolver?.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
			}
		if (args.size <= 1) alertDialog.setNeutralButton("Add folder") { _, _ ->
			val uri = "${File(args[0]).parent}/%"
			addToBlacklist(activity, listOf(uri))
			activity?.contentResolver?.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
		}


		return alertDialog.create()

	}

	companion object {
		fun newInstance(filePath: List<String>, notifyRemove: NotifyRemoveFun, listPos: Int): AddToBlacklistDialog =
			AddToBlacklistDialog().apply {
				this.args = filePath
				this.notifyRemove = notifyRemove
				this.listPos = listPos
			}
	}

}