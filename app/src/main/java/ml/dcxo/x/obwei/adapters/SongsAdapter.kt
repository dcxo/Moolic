package ml.dcxo.x.obwei.adapters

import android.os.Bundle
import android.view.*
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.load.resource.bitmap.*
import kotlinx.android.synthetic.main.item_song.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 02/11/2018 for XOXO
 */
class SongsAdapter: BaseAdapter<Song, SongsAdapter.SongViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
		SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))

	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.filePath == newItem.filePath
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.title == newItem.title
				&& oldItem.albumTitle == newItem.albumTitle
				&& oldItem.artistName == newItem.artistName
	override fun getChanges(oldItem: Song, newItem: Song): Bundle? {

		val b = Bundle()

		if (oldItem.title != newItem.title) b.putString(PayloadsKeys.songTitleKey, newItem.title)
		if (oldItem.artistName != newItem.artistName) b.putString(PayloadsKeys.artistNameKey, newItem.artistName)
		if (oldItem.getAlbumArtURI != newItem.getAlbumArtURI) b.putString(PayloadsKeys.albumArtUriKey, newItem.getAlbumArtURI)

		return if (b.isEmpty) null else b

	}

	override fun getSectionName(position: Int): String =
		data[position].title.trim()[0].toUpperCase().toString()

	inner class SongViewHolder(itemView: View): BaseViewHolder<Song>(itemView) {

		lateinit var song: Song
		var ambiColor = AmbiColor.NULL

		override fun bind(i: Song, position: Int) {

			song = i

			itemSongTitle.text = i.title
			artistName.text = i.artistName

			GlideApp.with(thumbnailAlbumArt).asAmbitmap()
				.load(i.getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_song)
				.transforms(RoundedCorners(4.dp), CenterCrop())
				.transition(GenericTransitionOptions<Ambitmap>().transition(android.R.anim.fade_in))
				.into(MukolorTarget(thumbnailAlbumArt) {
					ambiColor = it
				})

		}
		override fun changeViewWithPayloads(changes: Bundle) {

			var s = changes.getString(PayloadsKeys.songTitleKey)
			if (s != null) itemSongTitle.text = s
			s = changes.getString(PayloadsKeys.artistNameKey)
			if (s != null) artistName.text = s

			GlideApp.with(thumbnailAlbumArt).asAmbitmap()
				.load(data[adapterPosition].getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_song)
				.transforms(RoundedCorners(4.dp), CenterCrop())
				.into(MukolorTarget(thumbnailAlbumArt) {
					ambiColor = it
				})


		}
		override fun recycle() {
			ambiColor = AmbiColor.NULL
			GlideApp.with(itemView).clear(thumbnailAlbumArt)
		}

	}

	object PayloadsKeys {
		const val songTitleKey = "X_SONG_TITLE_X"
		const val artistNameKey = "X_ARTIST_NAME_X"
		const val albumArtUriKey = "X_ALBUM_ART_URI_X"
	}

}