package ml.dcxo.x.obwei.adapters

import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.*
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_song.*
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.dp

/**
 * Created by David on 28/12/2018 for XOXO
 */
class PlaylistTracksAdapter: BaseAdapter<Song, PlaylistTracksAdapter.PlaylistTrackVH>() {

	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.title == newItem.title
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.title == newItem.title
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistTrackVH =
		PlaylistTrackVH( LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false) )

	inner class PlaylistTrackVH(itemView: View): BaseViewHolder<Song>(itemView) {

		override fun bind(i: Song, position: Int) {

			itemSongTitle.text = i.title
			artistName.text = i.artistName

			val roundedCorners = RoundedCorners(4.dp)
			val errBuilder = Glide.with(thumbnailAlbumArt).asBitmap()
				.load(R.drawable.drawable_error_album_art_song)
				.apply(RequestOptions().transform(roundedCorners))

			Glide.with(thumbnailAlbumArt).asBitmap()
				.load(i.getAlbumArtURI)
				.error(errBuilder)
				.apply(RequestOptions.bitmapTransform(
					MultiTransformation(roundedCorners, CenterCrop())
				))
				.transition(BitmapTransitionOptions.withCrossFade(440))
				.into(thumbnailAlbumArt)

		}
		override fun recycle() {
			Glide.with(itemView).clear(thumbnailAlbumArt)
		}

	}

}