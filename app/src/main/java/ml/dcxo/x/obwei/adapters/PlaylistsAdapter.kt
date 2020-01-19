package ml.dcxo.x.obwei.adapters

import android.graphics.Typeface
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.shape.*
import kotlinx.android.synthetic.main.item_playlist.*
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Playlist
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.dp

/**
 * Created by David on 18/12/2018 for XOXO
 */
class PlaylistsAdapter: BaseAdapter<Playlist, PlaylistsAdapter.PlaylistVH>() {

	override fun areItemTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
		oldItem.trackList == newItem.trackList
	override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
		oldItem.trackList.size == newItem.trackList.size

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistVH =
		PlaylistVH( LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false) )

	inner class PlaylistVH(itemView: View): BaseViewHolder<Playlist>(itemView) {

		override fun bind(i: Playlist, position: Int) {

			if (i.id == -1) {
				itemView.background =
					MaterialShapeDrawable(ShapePathModel().apply {
						bottomLeftCorner = RoundedCornerTreatment(8.dp.toFloat())
						bottomRightCorner = RoundedCornerTreatment(8.dp.toFloat())
						topLeftCorner = RoundedCornerTreatment(8.dp.toFloat())
						topRightCorner = RoundedCornerTreatment(8.dp.toFloat())
					}).apply {
						setTint(ColorUtils.setAlphaComponent(ContextCompat.getColor(itemView.context, R.color.colorPrimary), 255*3/4))
					}
			}
			playlistName.text = i.name

		}
		override fun recycle() {
		}
	}

}