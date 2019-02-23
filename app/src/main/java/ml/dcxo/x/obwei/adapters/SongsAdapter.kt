package ml.dcxo.x.obwei.adapters

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_song.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.utils.GlideApp
import ml.dcxo.x.obwei.utils.dp
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

			songTitle.text = i.title
			artistName.text = i.artistName

			GlideApp.with(thumbnailAlbumArt).asBitmap()
				.load(i.getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_song)
				.transforms(RoundedCorners(4.dp), CenterCrop())
				.transition(BitmapTransitionOptions.withCrossFade(440))
				.into(object: BitmapImageViewTarget(thumbnailAlbumArt) {
					override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
						super.onResourceReady(resource, transition)

						launch(Dispatchers.Default) { ambiColor = AmbiColor(resource) }

					}

					override fun onLoadFailed(errorDrawable: Drawable?) {
						super.onLoadFailed(errorDrawable)

						ambiColor = AmbiColor.NULL

					}
				})

		}
		override fun changeViewWithPayloads(changes: Bundle) {

			var s = changes.getString(PayloadsKeys.songTitleKey)
			if (s != null) songTitle.text = s
			s = changes.getString(PayloadsKeys.artistNameKey)
			if (s != null) artistName.text = s

			GlideApp.with(thumbnailAlbumArt).asBitmap()
				.load(data[adapterPosition].getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_song)
				.transforms(RoundedCorners(4.dp), CenterCrop())
				.transition(BitmapTransitionOptions.withCrossFade(440))
				.into(object: BitmapImageViewTarget(thumbnailAlbumArt) {
					override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
						super.onResourceReady(resource, transition)

						launch(Dispatchers.Default) { ambiColor = AmbiColor(resource) }

					}

					override fun onLoadFailed(errorDrawable: Drawable?) {
						super.onLoadFailed(errorDrawable)

						ambiColor = AmbiColor.NULL

					}
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