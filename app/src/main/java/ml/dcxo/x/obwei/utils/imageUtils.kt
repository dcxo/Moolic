package ml.dcxo.x.obwei.utils

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import ml.dcxo.x.obwei.AmbiColor

/**
 * Created by David on 02/11/2018 for XOXO
 */
val statusBarSize: Int; get() {
	var result = 0
	val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
	if (resourceId > 0) result = Resources.getSystem().getDimensionPixelSize(resourceId)
	return result
}

val Int.dp; get() = this * Resources.getSystem().displayMetrics.density.toInt()

class MarginDecor(
	private var dx: Int = statusBarSize/2,
	private var dy: Int = dx
): RecyclerView.ItemDecoration() {
	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		super.getItemOffsets(outRect, view, parent, state)

		outRect.offset(dx, dy)

	}
}

fun generateAlphaGradient(color: Int): GradientDrawable {

	return GradientDrawable(
		GradientDrawable.Orientation.TOP_BOTTOM,
		intArrayOf(
			ColorUtils.setAlphaComponent( color, 0 ),
			ColorUtils.setAlphaComponent( color, 191 ),
			ColorUtils.setAlphaComponent( color, 255 )
		)
	)

}
fun generateAmbiColorGradient(ambiColor: AmbiColor): GradientDrawable {
	return GradientDrawable(
		GradientDrawable.Orientation.TOP_BOTTOM,
		intArrayOf(
			ambiColor.secondaryColor,
			ambiColor.primaryColor,
			ambiColor.primaryColor
		)
	)
}
fun makeBackgroundDrawableForSeekBar(i: Int = "#88000000".toColorInt()) =
	ShapeDrawable(object : Shape() {
		override fun draw(canvas: Canvas?, paint: Paint?) {

			canvas?.let {

				val halfHeight = height / 2

				paint?.color = i

				val r = RectF(-halfHeight, 0f, width + halfHeight, height)
				it.drawRoundRect(r, halfHeight, halfHeight, paint)

			}

		}
	})
fun makeThumbDrawableForSeekBar(@ColorInt c: Int, radius: Float = 5.dp.toFloat()): ShapeDrawable =
	ShapeDrawable(object : Shape() {

		override fun draw(canvas: Canvas?, paint: Paint?) {

			canvas?.apply {

				paint?.style = Paint.Style.FILL
				paint?.color = c
				drawCircle(0f, 0f, radius, paint)

			}

		}

	})

@GlideModule class ObweiAppGlideModule: AppGlideModule()

