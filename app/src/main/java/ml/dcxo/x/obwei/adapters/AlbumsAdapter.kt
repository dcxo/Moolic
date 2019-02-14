package ml.dcxo.x.obwei.adapters

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.item_album.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.utils.generateAlphaGradient
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

			Glide.with(thumbnailAlbumArt).asBitmap().load(i.getAlbumArtURI)
				.error(Glide.with(thumbnailAlbumArt).asBitmap().load(R.drawable.drawable_error_album_art_album))
				.apply(
					RequestOptions().placeholder(R.drawable.drawable_error_album_art_album)
						.centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
				)
				.transition(BitmapTransitionOptions.withCrossFade(440))
				.listener(object: RequestListener<Bitmap> {
					override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean = false

					override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

						launch {
							if (resource != null) {
								val ambiColor =
									withContext(Dispatchers.Default) { AmbiColor(resource) }

								thumbnailAlbumArt.setImageBitmap(resource)
								albumTitle.setTextColor(ambiColor.primaryTextColor)
								artistName.setTextColor(ambiColor.primaryTextColor)
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									val gradient1 = generateAlphaGradient(Color.DKGRAY)
									val gradient = generateAlphaGradient(ambiColor.primaryColor)
									val td = TransitionDrawable(arrayOf(gradient1, gradient))
									thumbnailAlbumArt.foreground = td.apply { startTransition(440) }
								}

							} else {
								thumbnailAlbumArt.setImageResource(R.drawable.drawable_error_album_art_album)
								albumTitle.setTextColor(Color.LTGRAY)
								artistName.setTextColor(Color.LTGRAY)
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
									thumbnailAlbumArt.foreground =
											generateAlphaGradient(Color.DKGRAY)
							}
						}

						return false
					}

				})
				.into(thumbnailAlbumArt)

		}
		override fun changeViewWithPayloads(changes: Bundle) {

			var s = changes.getString(albumTitleKey)
			if (s != null) albumTitle.text = s
			s = changes.getString(artistNameKey)
			if (s != null) artistName.text = s

			Glide.with(thumbnailAlbumArt).asBitmap().load(data[adapterPosition].getAlbumArtURI)
				.error(Glide.with(thumbnailAlbumArt).asBitmap().load(R.drawable.drawable_error_album_art_album))
				.apply( RequestOptions().placeholder(R.drawable.drawable_error_album_art_album).centerCrop() )
				.transition(BitmapTransitionOptions.withCrossFade(440))
				.listener(object: RequestListener<Bitmap> {
					override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean = false

					override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

						launch {
							if (resource != null) {
								val ambiColor =
									withContext(Dispatchers.Default) { AmbiColor(resource) }

								thumbnailAlbumArt.setImageBitmap(resource)
								albumTitle.setTextColor(ambiColor.primaryTextColor)
								artistName.setTextColor(ambiColor.primaryTextColor)
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									val gradient1 = generateAlphaGradient(Color.DKGRAY)
									val gradient = generateAlphaGradient(ambiColor.primaryColor)
									val td = TransitionDrawable(arrayOf(gradient1, gradient))
									thumbnailAlbumArt.foreground = td.apply { startTransition(440) }
								}

							} else {
								thumbnailAlbumArt.setImageResource(R.drawable.drawable_error_album_art_album)
								albumTitle.setTextColor(Color.LTGRAY)
								artistName.setTextColor(Color.LTGRAY)
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
									thumbnailAlbumArt.foreground =
											generateAlphaGradient(Color.DKGRAY)
							}
						}

						return false
					}

				})
				.into(thumbnailAlbumArt)

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