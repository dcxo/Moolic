package ml.dcxo.x.obwei.adapters

import android.graphics.Bitmap
import android.os.Build
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.utils.generateAlphaGradient
import ml.dcxo.x.obwei.viewModel.Album

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class AlbumsAdapter: BaseAdapter<Album, AlbumsAdapter.AlbumViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder =
		AlbumViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false) )

	override fun areItemTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.trackList == newItem.trackList


	inner class AlbumViewHolder(itemView: View): BaseViewHolder<Album>(itemView) {

		var j: Job? = null

		override fun bind(i: Album, position: Int, click: ((Album, Int) -> Unit)?, longClick: ((Album, Int) -> Unit)?) {
			super.bind(i, position, click, longClick)

			itemView.albumTitle.text = i.title
			itemView.artistName.text = i.artistName

			Glide.with(itemView.context).load(i.getAlbumArtURI).asBitmap()
				//.transcode(TranscoderAmBitmap(::ambiColorCallback), Bitmap::class.java)
				.into(object: BitmapImageViewTarget(itemView.thumbnailAlbumArt){

					override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {

						if (resource != null) j = GlobalScope.launch(Dispatchers.Main) {
							val l = async { AmbiColor(resource) }
							ambiColorCallback(l.await())
						}

						super.onResourceReady(resource, glideAnimation)
					}

				})

		}
		override fun recycle() {
			j?.cancel()
		}

		private fun ambiColorCallback(ambiColor: AmbiColor) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				itemView.thumbnailAlbumArt.foreground = generateAlphaGradient(ambiColor)
			}
			itemView.albumTitle.setTextColor(ambiColor.primaryTextColor)
			itemView.artistName.setTextColor(ambiColor.primaryTextColor)

		}

	}


}