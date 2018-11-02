package ml.dcxo.x.obwei.adapters

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song_recycleritem.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class SongsAdapter: BaseAdapter<Song, SongsAdapter.SongViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
		SongViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.song_recycleritem, parent, false) )
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.filePath == newItem.filePath
	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id

	inner class SongViewHolder(itemView: View): BaseViewHolder<Song>(itemView) {

		override fun bind(i: Song) {

			itemView.songTitle.text = i.title
			itemView.artistName.text = i.artistName

		}

	}

}