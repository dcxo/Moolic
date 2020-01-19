package ml.dcxo.x.obwei.ui.dialogs.bottomDialogs

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_song.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.*
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.*
import ml.dcxo.x.obwei.viewModel.*

typealias NotifyRemoveFun = (Int)->Unit
/**
 * Created by David on 12/01/2019 for XOXO
 */
class SongBottomDialog: BaseBottomDialog<Song>() {

	override fun Song.getTitle(): String = title
	override fun Song.getData(): ArrayList<Song> = arrayListOf(this)
	override fun Song.getPaths(): List<String> = listOf( this.filePath )

	override fun hideViews(view: View) {
		super.hideViews(view)

		view.editMetadataOption.visibility = View.VISIBLE

	}

	companion object {

		fun newInstance(item: Song, baseFragment: BaseNavFragment<*,*>, listPos: Int): SongBottomDialog =
			SongBottomDialog().apply {
			this.item = item
			this.notifyRemove = baseFragment.notifyRemove
			this.uiInteractions = baseFragment.uiInteractions
			this.listPos = listPos
		}

	}

}