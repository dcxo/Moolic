package ml.dcxo.x.obwei.adapters

import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_album.*
import kotlinx.android.synthetic.main.item_album.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Album

/**
 * Created by David on 02/11/2018 for XOXO
 */
class AlbumsAdapter: BaseAdapter<Album, AlbumsAdapter.AlbumViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder =
		AlbumViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false) )

	override fun areItemTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
		oldItem.title == newItem.title
				&& oldItem.artistName == newItem.artistName
				&& oldItem.getAlbumArtURI == newItem.getAlbumArtURI
	override fun getChanges(oldItem: Album, newItem: Album): Bundle? {

		val b = Bundle()

		if (oldItem.title != newItem.title) b.putString(albumTitleKey, newItem.title)
		if (oldItem.artistName != newItem.artistName) b.putString(artistNameKey, newItem.artistName)
		b.putBoolean(albumArtUriKey, true)

		return if (b.isEmpty) null else b

	}

	override fun getSectionName(position: Int): String =
		data[position].title.trim()[0].toUpperCase().toString()

	inner class AlbumViewHolder(itemView: View): BaseViewHolder<Album>(itemView) {

		override fun bind(i: Album, position: Int) {

			albumTitle.text = i.title
			artistName.text = i.artistName

			GlideApp.with(thumbnailAlbumArt)
				.asAmbitmap()
				.load(i.getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_album)
				.centerCrop()
				.into(object: MukolorTarget(thumbnailAlbumArt) {
					override fun onColorsReady(ambiColor: AmbiColor) {

						albumTitle.setTextColor(ambiColor.textColor)
						artistName.setTextColor(ambiColor.textColor)
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							val gradient1 = generateAlphaGradient(Color.DKGRAY)
							val gradient = generateAlphaGradient(ambiColor.color)
							val td = TransitionDrawable(arrayOf(gradient1, gradient))
							view.foreground = td.apply { startTransition(440) }
						}

					}
				})
		}
		override fun changeViewWithPayloads(changes: Bundle) {

			var s = changes.getString(albumTitleKey)
			if (s != null) albumTitle.text = s
			s = changes.getString(artistNameKey)
			if (s != null) artistName.text = s

			GlideApp.with(thumbnailAlbumArt)
				.asAmbitmap()
				.load(data[adapterPosition].getAlbumArtURI)
				.error(R.drawable.drawable_error_album_art_album)
				.centerCrop()
				.transition(GenericTransitionOptions<Ambitmap>().transition(android.R.anim.fade_in))
				.into(MukolorTarget(thumbnailAlbumArt) {

					albumTitle.setTextColor(it.textColor)
					artistName.setTextColor(it.textColor)

					val gradient = generateAlphaGradient(it.color)
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) view.foreground = gradient
					else itemView.bgForeground.background = gradient

				})

		}
		override fun recycle() {
			super.recycle()

			Glide.with(itemView).clear(thumbnailAlbumArt)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				thumbnailAlbumArt.foreground = generateAlphaGradient(Color.DKGRAY)
			}

		}

	}

	companion object {
		const val albumTitleKey = "X_ALBUM_TITLE_X"
		const val artistNameKey = "X_ARTIST_NAME_X"
		const val albumArtUriKey = "X_ALBUM_ART_X"
	}

}