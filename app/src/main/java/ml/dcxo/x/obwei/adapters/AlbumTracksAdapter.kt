package ml.dcxo.x.obwei.adapters

import android.graphics.Color
import android.view.*
import kotlinx.android.synthetic.main.item_queue_song.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 01/12/2018 for XOXO
 */
class AlbumTracksAdapter: BaseAdapter<Song, AlbumTracksAdapter.AlbumTrackVH>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumTrackVH =
		AlbumTrackVH( LayoutInflater.from(parent.context).inflate(R.layout.item_queue_song, parent, false) )

	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.id == newItem.id

	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.filePath == newItem.filePath

	inner class AlbumTrackVH(itemView: View): BaseViewHolder<Song>(itemView) {

		override fun bind(i: Song, position: Int) {

			positionInQueue.text = "${i.track%1000}"
			queueSongTitle.text = i.title

			positionInQueue.setTextColor(Color.GRAY)
			queueSongTitle.setTextColor(Color.GRAY)

		}
		override fun recycle() {
		}
	}

}