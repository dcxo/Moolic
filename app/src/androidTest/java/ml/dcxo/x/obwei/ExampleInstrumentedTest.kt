package ml.dcxo.x.obwei

import android.util.Log
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
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

	@Test
	fun useAppContext() {
		// Context of the app under test.
	}

	@Test
	fun orderTiming() {

		val c = InstrumentationRegistry.getInstrumentation().targetContext

		val milliAlbums = measureTimeMillis { AlbumsProvider.getAlbums(c) }
		val milliArtists = measureTimeMillis { ArtistsProvider.getArtists(c) }

		Log.d("Albums milliseconds", "$milliAlbums ms")
		Log.d("Artists milliseconds", "$milliArtists ms")

	}

}
