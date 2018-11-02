package ml.dcxo.x.obwei.adapters

import android.view.*
import kotlinx.android.synthetic.main.album_recycleritem.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Album

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class AlbumsAdapter: BaseAdapter<Album, AlbumsAdapter.AlbumViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder =
		AlbumViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.album_recycleritem, parent, false) )

	override fun areItemTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.trackList == newItem.trackList


	inner class AlbumViewHolder(itemView: View): BaseViewHolder<Album>(itemView) {

		override fun bind(i: Album) {

			itemView.albumTitle.text = i.title
			itemView.artistName.text = i.artistName

		}

	}

}