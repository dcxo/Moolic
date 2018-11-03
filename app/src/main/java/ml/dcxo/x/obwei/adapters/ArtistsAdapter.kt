package ml.dcxo.x.obwei.adapters

import android.view.*
import kotlinx.android.synthetic.main.artist_recycleritem.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Artist

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class ArtistsAdapter: BaseAdapter<Artist, ArtistsAdapter.ArtistViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder =
		ArtistViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.artist_recycleritem, parent, false) )
	override fun areItemTheSame(oldItem: Artist, newItem: Artist): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean = oldItem.trackList == newItem.trackList

	inner class ArtistViewHolder(itemView: View): BaseViewHolder<Artist>(itemView) {

		override fun bind(i: Artist, position: Int, click: ((Artist, Int) -> Unit)?, longClick: ((Artist, Int) -> Unit)?) {
			super.bind(i, position, click, longClick)

			itemView.artistName.text = i.name

		}
		override fun recycle() {

		}

	}

}