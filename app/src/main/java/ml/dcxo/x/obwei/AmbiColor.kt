package ml.dcxo.x.obwei

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import java.lang.Math.abs
import java.lang.Math.min

/**
 * Created by David on 02/11/2018 for XOXO
 */
class AmbiColor private constructor() {

	constructor(bitmap: Bitmap?) : this() {

		if (bitmap == null) return

		val palette = Palette.from(bitmap).generate()

		selectSwatch( palette.vibrantSwatch )
		selectSwatch( palette.mutedSwatch )
		selectSwatch( palette.lightMutedSwatch )
		selectSwatch( palette.lightVibrantSwatch )
		selectSwatch( palette.dominantSwatch )

		if (!secondarySelected) forceSecondary()

	}

	private lateinit var primarySwatch: Palette.Swatch

	var primaryColor: Int = Color.WHITE
	var primaryTextColor: Int = Color.BLACK
	var primaryBgColor: Int = Color.BLACK
	var secondaryColor: Int = Color.WHITE
	var secondaryTextColor: Int = Color.BLACK

	private var secondarySwatch: Palette.Swatch? = null
	private var primarySelected = false
	private var secondarySelected = false

	private fun primarySwatch(s: Palette.Swatch) {

		primarySwatch = s
		primaryColor = s.rgb
		primaryTextColor = s.titleTextColor
		primarySelected = true

		val w = ColorUtils.calculateMinimumAlpha(primaryColor, Color.WHITE, 3f)
		val dg = ColorUtils.calculateMinimumAlpha(primaryColor, Color.DKGRAY, 3f)
		primaryBgColor = when {
			dg != -1 -> ColorUtils.setAlphaComponent(Color.DKGRAY, 255)
			w != -1  -> ColorUtils.setAlphaComponent(Color.WHITE, 255)
			else     -> ColorUtils.setAlphaComponent(Color.BLACK, 255)
		}

	}
	private fun secondarySwatch(s: Palette.Swatch) {

		val contrast = ColorUtils.calculateContrast(primaryColor, s.rgb)
		val dHue = min(abs(primarySwatch.hsl[0] - s.hsl[0]),360 - abs(primarySwatch.hsl[0] - s.hsl[0])) / 180

		if (dHue > 0.125f && dHue !in 0f..0.125f && contrast <= 4.5f) {
			secondaryColor = s.rgb
			secondaryTextColor = s.titleTextColor
			secondarySwatch = s
			secondarySelected = true
		}

	}
	private fun forceSecondary() {

		val degrees = 30f
		val hsl = primarySwatch.hsl.clone()
		when (hsl[0]) {
			in 0f..90f    -> hsl[0] += degrees
			in 91f..1800f -> hsl[0] -= degrees
			in 181f..270f -> hsl[0] += degrees
			in 271f..359f -> hsl[0] -= degrees
		}
		secondaryColor = ColorUtils.HSLToColor(hsl)

		val b = ColorUtils.calculateMinimumAlpha(Color.BLACK, secondaryColor, 3f)
		val w = ColorUtils.calculateMinimumAlpha(Color.WHITE, secondaryColor, 3f)
		secondaryTextColor = if (b != -1) {
			ColorUtils.setAlphaComponent(Color.BLACK, b)
		} else {
			ColorUtils.setAlphaComponent(Color.WHITE, w)
		}

	}
	private fun selectSwatch(s: Palette.Swatch?) {
		s?.let {

			if (!primarySelected) primarySwatch(s)
			else if (!secondarySelected) secondarySwatch(s)
			else return

		}
	}

	override fun equals(other: Any?): Boolean =
		other is AmbiColor && primaryColor != other.primaryColor &&
				secondaryColor != other.secondaryColor && primaryTextColor != other.primaryTextColor &&
				secondaryTextColor != other.secondaryTextColor

	companion object {

		val NULL = AmbiColor()

	}

}
