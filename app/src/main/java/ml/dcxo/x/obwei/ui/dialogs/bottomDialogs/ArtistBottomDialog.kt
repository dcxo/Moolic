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
import ml.dcxo.x.obwei.ui.dialogs.AddToBlacklistDialog
import ml.dcxo.x.obwei.ui.dialogs.RemoveFromDiskDialog
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 12/01/2019 for XOXO
 */
class ArtistBottomDialog: BaseBottomDialog<Artist>() {

	override fun Artist.getTitle(): String = name
	override fun Artist.getData(): ArrayList<Song> = trackList
	override fun Artist.getPaths(): List<String> = trackList.map { it.filePath }

	override fun hideViews(view: View) {
		super.hideViews(view)

		view.goToAlbumOption.visibility = View.GONE
		view.goToArtistOption.visibility = View.GONE

	}

	companion object {

		fun newInstance(item: Artist, baseFragment: BaseNavFragment<*, *>, listPos: Int): ArtistBottomDialog =
			ArtistBottomDialog().apply {
				this.item = item
				this.notifyRemove = baseFragment.notifyRemove
				this.uiInteractions = baseFragment.uiInteractions
				this.listPos = listPos
			}

	}

}