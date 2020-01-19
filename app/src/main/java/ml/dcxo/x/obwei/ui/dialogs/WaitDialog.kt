package ml.dcxo.x.obwei.ui.dialogs

import android.content.Context
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import ml.dcxo.x.obwei.R
import java.lang.Exception


/**
 * Created by David on 09/02/2019 for ObweiX
 */
fun waitDialog(ctx: Context?): AlertDialog {

	if (ctx == null) throw Exception("EAAAAAAAAAAAA")

	val v = ProgressBar(ctx)
	v.id = R.id.topContainer

	return AlertDialog.Builder(ctx, R.style.Theme_AppCompat_DayNight_Dialog)
		.setView(v)
		.setCancelable(false)
		.create()

}