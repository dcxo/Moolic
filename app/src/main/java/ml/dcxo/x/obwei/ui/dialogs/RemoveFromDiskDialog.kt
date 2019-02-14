package ml.dcxo.x.obwei.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.utils.removeFromDisk
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 04/12/2018 for XOXO
 */
class RemoveFromDiskDialog: DialogFragment() {

	lateinit var notifyRemove: NotifyRemoveFun

	private var listPos = -1
	private var args = listOf<Song>()

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

		val alertDialog = AlertDialog.Builder(activity!!)
			.setTitle(R.string.remove_from_disk)
			.setNegativeButton(R.string.cancel) {_, _ ->  }
			.setPositiveButton(R.string.accept) {_, _ ->
				if (removeFromDisk(activity, args))
					notifyRemove(listPos)
			}

		return alertDialog.create()
	}

	companion object {
		fun newInstance(filePaths: List<Song>, notifyRemove: NotifyRemoveFun, listPos: Int): RemoveFromDiskDialog =
			RemoveFromDiskDialog().apply {
				this.args = filePaths
				this.listPos = listPos
				this.notifyRemove = notifyRemove
			}
	}

}