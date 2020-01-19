package ml.dcxo.x.obwei.adapters

import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_albumart.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 19/02/2019 for ObweiX
 */
class AlbumArtsAdapter: BaseAdapter<Song, AlbumArtsAdapter.AlbumArtsVH>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumArtsVH =
		AlbumArtsVH( LayoutInflater.from(parent.context).inflate(R.layout.fragment_albumart, parent, false) )

	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.filePath == newItem.filePath
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.title == newItem.title
				&& oldItem.albumTitle == newItem.albumTitle
				&& oldItem.artistName == newItem.artistName

	inner class AlbumArtsVH(itemView: View): BaseViewHolder<Song>(itemView) {
		override fun bind(i: Song, position: Int) {

			Glide.with(itemView).asBitmap().load(i.getAlbumArtURI)
				.apply(RequestOptions.centerCropTransform())
				.error(Glide.with(itemView).asBitmap().load(R.drawable.drawable_error_album_art_song))
				.into(albumArt)

		}
	}

}