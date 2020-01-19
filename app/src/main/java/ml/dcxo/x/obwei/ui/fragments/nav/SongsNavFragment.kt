package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.*
import kotlinx.android.synthetic.main.fragment_nav.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.SongsAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.SongBottomDialog
import ml.dcxo.x.obwei.utils.dp
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 02/11/2018 for XOXO
 */
class SongsNavFragment: BaseNavFragment<Song, SongsAdapter>() {

	override val click: ((Song, Int) -> Unit)? = { song, position ->
		mActivity?.hideKeyboard()

		uiInteractions?.onSongSelected(
			song,
			getLiveData().value ?: throw IllegalStateException(),
			position
		)
	}
	override val longClick: ((Song, Int) -> Unit)? = {song, i ->
		mActivity?.hideKeyboard()

		fragmentManager?.let { fm ->
			SongBottomDialog.newInstance(song, this, i).show(fm, "SBD")
		}
	}

	override val touchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

		override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean =
			false

		override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

			val songs = arrayListOf( (viewHolder as SongsAdapter.SongViewHolder).song )
			uiInteractions?.onAddToQueue(songs, getLiveData().value ?: throw IllegalStateException(), viewHolder.adapterPosition)
			view?.getAdapter()?.notifyItemChanged(viewHolder.adapterPosition)

		}

		override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
			val view = (viewHolder as SongsAdapter.SongViewHolder).itemView
			val y = 32.dp
			val x = (view.bottom - view.top - y)/2
			val childWidth = 2f*x+y+4.dp
			return childWidth/view.width
		}
		override fun onChildDrawOver(
			c: Canvas,
			recyclerView: RecyclerView,
			viewHolder: RecyclerView.ViewHolder,
			dX: Float,
			dY: Float,
			actionState: Int,
			isCurrentlyActive: Boolean
		) {


			val view = (viewHolder as SongsAdapter.SongViewHolder).itemView
			val y = 32.dp
			val height = view.bottom - view.top
			val x = (height - y)/2
			val maxX = Math.min(dX, 2f*x+y+4.dp)
			val clipRect = RectF(0f, view.top.toFloat(), maxX, view.bottom.toFloat())
			c.clipRect(clipRect)
			c.drawColor(viewHolder.ambiColor.color)

			val rect = RectF(maxX - 4.dp, view.top.toFloat(), maxX + 4.dp, view.bottom.toFloat())
			val paint = Paint().apply {
				style = Paint.Style.FILL
				color = Color.WHITE
			}
			c.drawRoundRect(rect, 4.dp.toFloat(), 4.dp.toFloat(), paint)
			c.clipRect(clipRect.apply {
				right -= 4.dp
			})

			val drawable = resources.getDrawable(R.drawable.icon_skip_next, null)
			val drawableRect = Rect(x, x+view.top, x+y, x+view.top+y)
			drawable.setTint(viewHolder.ambiColor.textColor)
			drawable.bounds = drawableRect
			drawable.draw(c)

			super.onChildDraw(
				c,
				recyclerView,
				viewHolder,
				Math.min(dX, 2f*x+y+4.dp),
				dY,
				actionState,
				isCurrentlyActive
			)
		}

	})

	override fun filterData(model: Song, query: String): Boolean =
		model.title.contains(query, true)
	override fun getToolbarTitle(): String = "${getLiveData().value?.size ?: "No"} Songs"
	override fun getLiveData(): LiveData<Tracklist> =
		mActivity?.obweiViewModel?.getSongs() ?: MutableLiveData()
	override fun getAdapterAndLayoutManager(context: Context): Pair<SongsAdapter, RecyclerView.LayoutManager> =
		SongsAdapter() to LinearLayoutManager(context, RecyclerView.VERTICAL, false)


	override fun editOnCreateView(view: View) {
		super.editOnCreateView(view)
		view.fabNav.show()
	}

}