package ml.dcxo.x.obwei.utils

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by David on 02/11/2018 for ObweiX
 */
val getStatusBarHeight: Int; get() {
	var result = 0
	val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
	if (resourceId > 0) result = Resources.getSystem().getDimensionPixelSize(resourceId)
	return result
}

val Int.dp; get() = this * Resources.getSystem().displayMetrics.density.toInt()

class MarginDecor(private var dx: Int = getStatusBarHeight/2, private var dy: Int = getStatusBarHeight/2): RecyclerView.ItemDecoration() {
	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		super.getItemOffsets(outRect, view, parent, state)

		outRect.offset(dx, dy)

	}
}