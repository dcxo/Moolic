package ml.dcxo.x.obwei.base

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_song.view.*
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.*
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.AlbumMetadataEditorFragment
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.SongMetadataEditorFragment
import ml.dcxo.x.obwei.utils.blacklistKey
import ml.dcxo.x.obwei.utils.metadataEditorFragmentTag
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 29/01/2019 for ObweiX
 */
abstract class BaseBottomDialog<Item: Model> : BottomSheetDialogFragment() {

	lateinit var item: Item
	lateinit var baseNavFragment: BaseNavFragment<*,*>

	var listPos = 0

	protected lateinit var notifyRemove: NotifyRemoveFun
	protected var uiInteractions: UIInteractions? = null
	private val playlists: ArrayList<Playlist>; get() {
		return (activity as? UniqueActivity)?.obweiViewModel?.getPlaylist()?.value ?: arrayListOf()
	}

	abstract fun Item.getTitle(): String
	abstract fun Item.getData(): ArrayList<Song>
	abstract fun Item.getPaths(): List<String>

	open fun hideViews(view: View) {
		view.editMetadataOption.visibility = View.GONE
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

		val v = inflater.inflate(R.layout.dialog_bottom_song, container, false)

		hideViews(v)

		v.dbSongTitle.text = item.getTitle()
		v.addToQueueOption.setOnClickListener {
			uiInteractions?.onAddToQueue(
				item.getData(),
				(activity as? UniqueActivity)?.obweiViewModel?.getSongs()?.value ?: Tracklist()
				, listPos)
			Toast.makeText(
				v.context,
				resources.getQuantityString(R.plurals.addedToQueue, 1),
				Toast.LENGTH_SHORT
			).show()
			dialog?.dismiss()
		}
		v.addToBlacklistOption.setOnClickListener {
			fragmentManager?.let { fm ->
				AddToBlacklistDialog
					.newInstance(item.getPaths(), notifyRemove, listPos)
					.show(fm, blacklistKey)
				dialog?.dismiss()
			}
		}
		v.addToPlaylistOption.setOnClickListener {
			context?.let { it1 ->
				addToPlaylistDialog(it1, playlists, item.getData()).show()
				dialog?.dismiss()
			}
		}
		v.goToAlbumOption.setOnClickListener {
			(activity as? UniqueActivity)?.showAlbum(item.getData()[0])
			dialog?.dismiss()
		}
		v.goToArtistOption.setOnClickListener {
			(activity as? UniqueActivity)?.showArtist(item.getData()[0])
			dialog?.dismiss()
		}
		v.removeFromDiskOption.setOnClickListener {
			fragmentManager?.let { fm ->
				RemoveFromDiskDialog.newInstance(item.getData(), notifyRemove, listPos).show(fm, "")
				dialog?.dismiss()
			}
		}
		v.editMetadataOption.setOnClickListener {

			if (item is Artist) {
				dialog?.dismiss()
				return@setOnClickListener
			}

			val fragment = if (item is Song) SongMetadataEditorFragment().apply {obj = item as Song}
			else AlbumMetadataEditorFragment().apply {obj = item as Album}

			fragmentManager?.beginTransaction()
				?.replace(
					R.id.metadataEditorContainer,
					fragment,
					metadataEditorFragmentTag
				)?.addToBackStack("I_WANT_THIS_TO_WORK")
				?.commit()

			dialog?.dismiss()
		}

		return v

	}
	override fun onDestroyView() {

		view?.addToQueueOption?.setOnClickListener(null)
		view?.addToPlaylistOption?.setOnClickListener(null)
		view?.addToBlacklistOption?.setOnClickListener(null)
		view?.goToAlbumOption?.setOnClickListener(null)
		view?.goToArtistOption?.setOnClickListener(null)
		view?.removeFromDiskOption?.setOnClickListener(null)
		view?.editMetadataOption?.setOnClickListener(null)

		super.onDestroyView()
	}

}