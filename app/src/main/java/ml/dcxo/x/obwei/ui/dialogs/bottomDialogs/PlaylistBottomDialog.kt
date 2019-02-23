package ml.dcxo.x.obwei.ui.dialogs.bottomDialogs

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_song.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.utils.editPlaylistName
import ml.dcxo.x.obwei.utils.removePlaylist
import ml.dcxo.x.obwei.viewModel.Playlist
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 19/02/2019 for ObweiX
 */
class PlaylistBottomDialog: BottomSheetDialogFragment() {

	lateinit var item: Playlist

	var listPos = 0
	var uiInteractions: UIInteractions? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

		val v = inflater.inflate(R.layout.dialog_bottom_song, container, false)

		v.songTitle.text = item.name
		v.addToBlacklistOption.visibility = View.GONE
		v.addToPlaylistOption.visibility = View.GONE
		v.goToAlbumOption.visibility = View.GONE
		v.goToArtistOption.visibility = View.GONE
		v.addToQueueOption.setOnClickListener {
			uiInteractions?.onAddToQueue(item.trackList, Tracklist(), listPos)
			Toast.makeText(
				v.context,
				resources.getQuantityString(R.plurals.addedToQueue, 1),
				Toast.LENGTH_SHORT
			).show()
			dialog?.dismiss()
		}
		v.removeFromDiskOption.setOnClickListener {
			if (item.filePath.isNotBlank())
				context?.let { it1 ->
					AlertDialog.Builder(it1)
						.setPositiveButton(R.string.accept) {_, _ -> removePlaylist(it1, item) }
						.setNegativeButton(R.string.cancel) {_, _ ->  }
						.setTitle(R.string.remove_from_disk)
						.show()
				}

			dialog?.dismiss()

		}
		v.editMetadataOption.setOnClickListener {

			val editText = EditText(context).apply {
				setText(item.name, TextView.BufferType.EDITABLE)
			}

			context?.let { it1 ->
				AlertDialog.Builder(it1)
					.setView(editText)
					.setPositiveButton(R.string.accept) {_, _ -> editPlaylistName(it1, item, "${editText.text}") }
					.setNegativeButton(R.string.cancel) {_, _ ->  }
					.show()
			}

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