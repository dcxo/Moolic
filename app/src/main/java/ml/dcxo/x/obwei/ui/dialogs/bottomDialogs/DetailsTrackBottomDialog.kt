package ml.dcxo.x.obwei.ui.dialogs.bottomDialogs

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_song.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.*
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.ui.dialogs.*
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.AlbumMetadataEditorFragment
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.SongMetadataEditorFragment
import ml.dcxo.x.obwei.utils.metadataEditorFragmentTag
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 12/01/2019 for XOXO
 */
class DetailsTrackBottomDialog: BaseBottomDialog<Song>() {

	override fun Song.getTitle(): String = title
	override fun Song.getData(): ArrayList<Song> = arrayListOf(this)
	override fun Song.getPaths(): List<String> = arrayListOf(filePath)

	override fun hideViews(view: View) {
		super.hideViews(view)

		view.editMetadataOption.visibility = View.VISIBLE
		view.goToArtistOption.visibility = View.GONE
		view.goToAlbumOption.visibility = View.GONE

	}

	companion object {

		fun newInstance(item: Song, baseFragment: BaseDetailsFragment<*>, listPos: Int): DetailsTrackBottomDialog =
			DetailsTrackBottomDialog().apply {
				this.item = item
				this.notifyRemove = baseFragment.notifyRemove
				this.uiInteractions = baseFragment.uiInteractions
				this.listPos = listPos
			}
		fun newInstance(item: Song, notifyRemove: NotifyRemoveFun, uiInteractions: UIInteractions, listPos: Int): DetailsTrackBottomDialog =
			DetailsTrackBottomDialog().apply {
				this.item = item
				this.notifyRemove = notifyRemove
				this.uiInteractions = uiInteractions
				this.listPos = listPos
			}

	}

}