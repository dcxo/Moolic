package ml.dcxo.x.obwei.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.Settings

/**
 *
 * Created by David on 08/01/2019 for XOXO
 */
class EditBlacklistDialog: DialogFragment() {

	var update: (() -> Unit)? = null

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

		val settings = Settings[activity]
		val copy = arrayOf<String>() + settings.blacklist
		val s = copy.map {

			var s = it.removePrefix( Environment.getExternalStorageDirectory().absolutePath )
			if (it.endsWith("%")) s = s.removeSuffix("/%")

			return@map s

		}.toTypedArray()

		val dialog = AlertDialog.Builder(activity!!)
			.setTitle(getString(R.string.edit_blacklist))
			.setItems(s) { _, i -> sureBlacklistDialog(activity!!, copy, i, update).show() }
			.setPositiveButton("Ok") {
				dialog, _ -> dialog.dismiss()
			}
			.create()

		return dialog

	}

	companion object {
		fun newInstance(update: (() -> Unit)? = null): EditBlacklistDialog =
			EditBlacklistDialog().apply {
				this.update = update
			}
	}

}