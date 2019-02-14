package ml.dcxo.x.obwei.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_artist.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.utils.ArtistPicManager
import ml.dcxo.x.obwei.utils.generateAlphaGradient
import ml.dcxo.x.obwei.viewModel.Artist

/**
 * Created by David on 02/11/2018 for XOXO
 */
class ArtistsAdapter: BaseAdapter<Artist, ArtistsAdapter.ArtistViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder =
		ArtistViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false) )
	override fun areItemTheSame(oldItem: Artist, newItem: Artist): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean = oldItem.trackList == newItem.trackList

	override fun getSectionName(position: Int): String =
		data[position].name.trim()[0].toUpperCase().toString()

	inner class ArtistViewHolder(itemView: View): BaseViewHolder<Artist>(itemView) {

		var j: Job? = null

		override fun bind(i: Artist, position: Int) {


			artistName.text = i.name
			thumbnailAlbumArt.setImageResource(R.drawable.drawable_error_album_art_artist)

			artistName.setTextColor(Color.LTGRAY)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				thumbnailAlbumArt.foreground =
						generateAlphaGradient(Color.DKGRAY)

			j = launch {
				val artistPicURL = ArtistPicManager.getArtistPicURL(itemView.context, i.name)
				val url =
					artistPicURL.first ?: artistPicURL.second?.await()
						?.getAsJsonObject("artist")
						?.getAsJsonArray("image")?.get(3)
						?.asJsonObject?.get("#text")?.asString ?: ""
				Glide.with(thumbnailAlbumArt).asBitmap()
					.load(url)
					.error(Glide.with(thumbnailAlbumArt).asBitmap().load(R.drawable.drawable_error_album_art_artist))
					.transition(BitmapTransitionOptions.withCrossFade(440))
					.apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop())
					.into(object : BitmapImageViewTarget(thumbnailAlbumArt) {
						override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
							super.onResourceReady(resource, transition)

							val ambiColor = AmbiColor(resource)

							thumbnailAlbumArt.setImageBitmap(resource)
							artistName.setTextColor(ambiColor.primaryTextColor)
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
								val gradient1 = generateAlphaGradient(Color.DKGRAY)
								val gradient = generateAlphaGradient(ambiColor.primaryColor)
								val td = TransitionDrawable(arrayOf(gradient1, gradient))
								thumbnailAlbumArt.foreground =
										td.apply { startTransition(440) }
							}
							launch {
								if (artistPicURL.first == null) {
									itemView.context.openFileOutput("${i.name}.jpg", Context.MODE_PRIVATE)?.use { fos ->
										resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
									}
								}
							}

						}

						override fun onLoadFailed(errorDrawable: Drawable?) {
							super.onLoadFailed(errorDrawable)

							artistName.setTextColor(Color.LTGRAY)
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
								thumbnailAlbumArt.foreground =
										generateAlphaGradient(Color.DKGRAY)

						}
					})
			}



		}
		override fun recycle() {
			j?.cancel()
			Glide.with(itemView).clear(thumbnailAlbumArt)
		}

	}

}