package ml.dcxo.x.obwei

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.*
import androidx.palette.graphics.Palette
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

/**
 * Created by David on 07/03/2019 for ObweiX
 */
class AmbiColor private constructor() {

	var color = Color.LTGRAY; private set
	val textColor by lazy { computeTextColor(color) }
	val bgColor by lazy { computeBGColor(color) }

	private fun computeTextColor(color: Int): Int {

		val minWhiteAlpha = ColorUtils.calculateMinimumAlpha(color, Color.WHITE, 4.5f)
		val minBlackAlpha = ColorUtils.calculateMinimumAlpha(color, Color.BLACK, 4.5f)

		return when {
			minWhiteAlpha != -1 -> Color.WHITE
			minBlackAlpha != -1 -> Color.BLACK
			else                -> Color.BLACK
		}

	}
	private fun computeBGColor(color: Int): Int {

		val w = ColorUtils.calculateMinimumAlpha(color, Color.WHITE, 3f)
		val dg = ColorUtils.calculateMinimumAlpha(color, Color.DKGRAY, 3f)

		return when {
			dg != -1 -> Color.DKGRAY
			w != -1  -> Color.WHITE
			else     -> Color.BLACK
		}
	}

	override operator fun equals(other: Any?): Boolean {
		return other is AmbiColor && color == other.color
	}
	override fun hashCode(): Int {
		return color
	}

	companion object {
		val NULL = AmbiColor()
	}

	class Builder : CoroutineScope {

		override val coroutineContext: CoroutineContext = DispatcherAsyncTask
		private var bBitmap: Bitmap? = null
		private var color: Int? = null

		fun setBitmap(bitmap: Bitmap?): Builder {

			if (color != null) throw Exception("Color has been setted")
			bBitmap = bitmap?.scale(32, 32)

			return this
		}
		fun setColor(c: Int?): Builder {
			if (bBitmap != null) throw Exception("Bitmap has been setted")
			color = c
			return this
		}
		fun buildAsync() = async {

			val b = bBitmap ?: color ?: return@async AmbiColor()

			when (b) {
				is Bitmap -> {
					val ambiColor = AmbiColor()
					val palette = Palette.from(b).generate()
					palette.apply {

						ambiColor.color = swatches.maxBy {
							-Math.abs(it.hsl[1] - 1f) - Math.abs(it.hsl[2] - 0.5f)
						}?.rgb ?: Color.LTGRAY

					}
					return@async ambiColor
				}
				is Int -> return@async AmbiColor().apply { color = b }
				else -> return@async AmbiColor()
			}

		}

	}

}