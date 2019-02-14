package ml.dcxo.x.obwei

import android.os.Environment
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

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

		Log.d("EEEH UUUH", Environment.getExternalStorageDirectory().absolutePath)
		Log.d("EEEH UUUH", Environment.getExternalStorageDirectory().path)
		Log.d("EEEH UUUH", "${Environment.getExternalStorageDirectory().path}/")

	}

}
