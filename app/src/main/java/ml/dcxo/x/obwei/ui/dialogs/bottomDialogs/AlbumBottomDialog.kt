package ml.dcxo.x.obwei.ui.dialogs.bottomDialogs

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_song.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseBottomDialog
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.AddToBlacklistDialog
import ml.dcxo.x.obwei.ui.dialogs.RemoveFromDiskDialog
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 12/01/2019 for XOXO
 */
class AlbumBottomDialog: BaseBottomDialog<Album>() {

	override fun Album.getTitle(): String = title
	override fun Album.getData(): ArrayList<Song> = trackList
	override fun Album.getPaths(): List<String> = trackList.map { it.filePath }

	override fun hideViews(view: View) {
		super.hideViews(view)

		view.editMetadataOption.visibility = View.VISIBLE
		view.goToAlbumOption.visibility = View.GONE

	}

	companion object {

		fun newInstance(item: Album, baseFragment: BaseNavFragment<*, *>, listPos: Int): AlbumBottomDialog =
			AlbumBottomDialog().apply {
				this.item = item
				this.notifyRemove = baseFragment.notifyRemove
				this.uiInteractions = baseFragment.uiInteractions
				this.listPos = listPos
			}

	}

}