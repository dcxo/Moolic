package ml.dcxo.x.obwei

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.runBlocking
import java.lang.Math.*

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class AmbiColor() {

	constructor(bitmap: Bitmap, callback: (AmbiColor)->Unit) : this() {

		val palette = Palette.from(bitmap).generate()

		selectSwatch( palette.vibrantSwatch )
		selectSwatch( palette.mutedSwatch )
		selectSwatch( palette.lightMutedSwatch )
		selectSwatch( palette.lightVibrantSwatch )
		selectSwatch( palette.dominantSwatch )

	}

	private lateinit var primarySwatch: Palette.Swatch
	private lateinit var secondarySwatch: Palette.Swatch

	var primaryColor: Int = Color.WHITE
	var primaryTextColor: Int = Color.BLACK
	var secondaryColor: Int = Color.WHITE
	var secondaryTextColor: Int = Color.BLACK

	private var primarySelected = false
	private var secondarySelected = false

	private fun primarySwatch(s: Palette.Swatch) {

		primarySwatch = s
		primaryColor = s.rgb
		primaryTextColor = s.titleTextColor
		primarySelected = true

	}
	private fun secondarySwatch(s: Palette.Swatch) {

		val constrant = ColorUtils.calculateContrast(primaryColor, s.rgb)
		val dHue = min(abs(primarySwatch.hsl[0] - s.hsl[0]),360 - abs(primarySwatch.hsl[0] - s.hsl[0])) / 180

		if (dHue !in 0f..0.125f && dHue >= 0f) {
			secondaryColor = s.rgb
			secondaryTextColor = s.titleTextColor
			secondarySwatch = s
			secondarySelected = true
		}

	}
	private fun selectSwatch(s: Palette.Swatch?) {
		s?.let {

			if (!primarySelected) primarySwatch(s)
			else if (!secondarySelected) secondarySwatch(s)
			else return

		}
	}

}
