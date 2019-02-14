package ml.dcxo.x.obwei.adapters

import android.graphics.Color
import android.view.*
import androidx.core.graphics.toColorInt
import com.google.android.material.shape.*
import kotlinx.android.synthetic.main.item_queue_song.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.utils.dp
import ml.dcxo.x.obwei.viewModel.Song
import kotlin.properties.Delegates

/**
 * Created by David on 20/11/2018 for XOXO
 */
class QueueAdapter : BaseAdapter<Song, QueueAdapter.QueueVH>() {

	val bg = { color: Int ->

		val path = ShapePathModel().apply {
			val rcTreatment = RoundedCornerTreatment(4.dp.toFloat())
			topRightCorner = rcTreatment
			topLeftCorner = rcTreatment
			bottomRightCorner = rcTreatment
			bottomLeftCorner = rcTreatment
		}
		MaterialShapeDrawable(path).apply {
			setTint(color)
			shadowElevation = 0
		}

	}
	var currentPosition by Delegates.observable(0) {_, _, _ -> notifyDataSetChanged() }

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueVH =
		QueueVH( LayoutInflater.from(parent.context).inflate(R.layout.item_queue_song, parent, false) )
	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.filePath == newItem.filePath

	inner class QueueVH(itemView: View): BaseViewHolder<Song>(itemView) {

		var current = false

		override fun bind(i: Song, position: Int) {


			val a = (position - currentPosition)
			val c = if (a == 0) {
				current = true
				itemView.background = bg("#AAAAAA".toColorInt())
				Color.DKGRAY
			} else {
				current = false
				itemView.background = bg(Color.TRANSPARENT)
				Color.parseColor("#AAAAAA")
			}
			positionInQueue.text = a.toString()
			positionInQueue.setTextColor(c)
			songTitle.text = i.title
			songTitle.setTextColor(c)

		}
		override fun recycle() {}


	}
}