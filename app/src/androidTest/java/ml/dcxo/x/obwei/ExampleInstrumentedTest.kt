package ml.dcxo.x.obwei

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.*
import androidx.core.net.toUri
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import ml.dcxo.x.obwei.viewModel.Album
import ml.dcxo.x.obwei.viewModel.providers.AlbumsProvider
import ml.dcxo.x.obwei.viewModel.providers.ArtistsProvider
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class ExampleInstrumentedTest {

	@Test fun useAppContext() {
		runBlocking {

			val ctx = InstrumentationRegistry.getInstrumentation().targetContext

			for (i in 0..4) {
				val album = Album(id = 39)
				Log.d("Album HASH $i", "${album.title} || ${album.id}")
				val inputStream = ctx.contentResolver.openInputStream(album.getAlbumArtURI.toUri())
				val bitmapOriginal = BitmapFactory.decodeStream(inputStream)

				val ambiColor =
					AmbiColor.Builder()
						.setBitmap(bitmapOriginal)
						.buildAsync()
						.await()

				Log.d("Primary Color HASH $i", "\nr: ${ambiColor.color.red}\n" +
							"g: ${ambiColor.color.green}\n" +
							"b: ${ambiColor.color.blue}")
			}

		}
	}

	@Test fun orderTiming() {

		val c = InstrumentationRegistry.getInstrumentation().targetContext

		val milliAlbums = measureTimeMillis { AlbumsProvider.getAlbums(c) }
		val milliArtists = measureTimeMillis { ArtistsProvider.getArtists(c) }

		Log.d("Albums milliseconds", "$milliAlbums ms")
		Log.d("Artists milliseconds", "$milliArtists ms")

	}

	private fun getHash(bitmap: Bitmap?): Long {

		if (bitmap == null) {
			Log.d("BITMAP NULL HASH", "NULL")
			return 0
		}

		val pixels = IntArray(bitmap.width*bitmap.height)
		var hash = 31L
		bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

		for (pixel in pixels) {
			hash *= (31 + pixel)
		}

		return hash

	}

}
