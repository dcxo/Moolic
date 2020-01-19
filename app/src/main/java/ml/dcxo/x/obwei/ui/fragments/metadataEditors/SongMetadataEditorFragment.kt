package ml.dcxo.x.obwei.ui.fragments.metadataEditors

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_song_metadata_editor.view.*
import ml.dcxo.x.obwei.base.BaseMetadataEditorFragment
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.R
import org.jaudiotagger.tag.FieldKey

/**
 * Created by David on 31/01/2019 for ObweiX
 */
class SongMetadataEditorFragment: BaseMetadataEditorFragment<Song>() {

	override val layoutRes: Int = R.layout.fragment_song_metadata_editor

	override fun Song.setItemData(view: View) {

		view.mdSongTitle.editText?.setText(title, TextView.BufferType.EDITABLE)
		view.albumTitle.editText?.setText(albumTitle, TextView.BufferType.EDITABLE)
		view.artistName.editText?.setText(
			if (artistName == "<unknown>" || artistName.isEmpty()) "" else artistName,
			TextView.BufferType.EDITABLE
		)
		view.year.editText?.setText(
			if (year == 0) "" else year.toString(), TextView.BufferType.EDITABLE
		)
		view.trackNumber.editText?.setText(
			if (track == 0 ) "" else track.toString(), TextView.BufferType.EDITABLE
		)

	}
	override fun Song.getData(): List<String> = listOf(filePath)
	override fun getFieldsData(): Map<FieldKey, String> = mapOf(
		FieldKey.TITLE  to (view?.mdSongTitle?.editText?.text?.toString() ?: obj.title),
		FieldKey.ALBUM  to (view?.albumTitle?.editText?.text?.toString() ?: obj.albumTitle),
		FieldKey.ARTIST to (view?.artistName?.editText?.text?.toString() ?: obj.artistName),
		FieldKey.YEAR   to (view?.year?.editText?.text?.toString() ?: obj.year.toString()),
		FieldKey.TRACK  to (view?.trackNumber?.editText?.text?.toString() ?: obj.track.toString())
	)
	override fun Song.getAlbumId(): Int = albumId

}