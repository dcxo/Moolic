package ml.dcxo.x.obwei.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.*
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.item_artist.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
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
				val artistPicURL = getArtistPicURL(itemView.context, i.name)
				val url =
					artistPicURL.first ?: artistPicURL.second?.await()
						?.getAsJsonObject("artist")
						?.getAsJsonArray("image")?.get(3)
						?.asJsonObject?.get("#text")?.asString ?: ""

				GlideApp.with(thumbnailAlbumArt).asAmbitmap()
					.load(url)
					.error(R.drawable.drawable_error_album_art_artist)
					.centerCrop()
					.transition(GenericTransitionOptions<Ambitmap>().transition(android.R.anim.fade_in))
					.into(object : MukolorTarget(thumbnailAlbumArt) {

						override fun onColorsReady(ambiColor: AmbiColor) {

							artistName.setTextColor(ambiColor.textColor)

							val gradient = generateAlphaGradient(ambiColor.color)
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) thumbnailAlbumArt.foreground = gradient
							else itemView.bgForeground.background = gradient

						}
						override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {
							super.onResourceReady(resource, transition)

							launch(Dispatchers.IO) {
								if (artistPicURL.first == null) {
									itemView.context.openFileOutput(i.name, Context.MODE_PRIVATE)?.use { fos ->
										resource.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
									}
								}
							}

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