package ml.dcxo.x.obwei.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.AmbiColor

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

class MarginDecor(
	private var dx: Int = getStatusBarHeight/2,
	private var dy: Int = getStatusBarHeight/2
): RecyclerView.ItemDecoration() {
	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		super.getItemOffsets(outRect, view, parent, state)

		outRect.offset(dx, dy)

	}
}

class TranscoderAmBitmap(private var callback: (AmbiColor) -> Unit):
	ResourceTranscoder<Bitmap, Bitmap> {

	override fun getId(): String = this::class.java.simpleName
	override fun transcode(toTranscode: Resource<Bitmap>?): Resource<Bitmap> {
		toTranscode!!.let {

			val bitmap = it.get()
			val l = AmbiColor(bitmap, callback)
			callback(l)

			return it

		}
	}
}

fun generateAlphaGradient(ambiColor: AmbiColor): GradientDrawable {

	return GradientDrawable(
		GradientDrawable.Orientation.TOP_BOTTOM,
		intArrayOf(
			ColorUtils.setAlphaComponent( ambiColor.primaryColor, 0 ),
			ColorUtils.setAlphaComponent( ambiColor.primaryColor, 191 ),
			ColorUtils.setAlphaComponent( ambiColor.primaryColor, 255 )
		)
	)

}
