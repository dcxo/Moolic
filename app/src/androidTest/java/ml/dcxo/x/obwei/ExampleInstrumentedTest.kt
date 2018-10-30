package ml.dcxo.x.obwei

import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import ml.dcxo.x.obwei.providers.ArtistsProvider
import ml.dcxo.x.obwei.providers.SongsProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

	@Test
	fun useAppContext() {
		// Context of the app under test.
		val appContext = InstrumentationRegistry.getTargetContext()
		assertEquals("ml.dcxo.x.obwei", appContext.packageName)
	}

	@Test
	fun orderTiming() {

		val c = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
			val ss = SongsProvider.getSongs(c)
			val AS = ArtistsProvider.getArtists(ss)

		val l = measureNanoTime { AS.sortBy { it.name } }
		val k = measureNanoTime { AS.sortBy { it.name } }
		val j = measureNanoTime { AS.sortBy { it.name } }
		val h = measureNanoTime { AS.sortBy { it.name } }
		Log.d("NS L", "$l ns")
		Log.d("NS K", "$k ns")
		Log.d("NS J", "$j ns")
		Log.d("NS H", "$h ns")
		Log.d("NS MED", "${(l+k+j+h)/4} ns media")

	}

}
