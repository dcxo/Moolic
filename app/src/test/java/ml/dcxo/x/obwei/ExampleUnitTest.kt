package ml.dcxo.x.obwei

import org.junit.Test

import org.junit.Assert.*
import java.util.*
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

	@Test
	fun addition_isCorrect() {
		assertEquals(4, 2 + 2)
	}

	@Test
	fun orderTiming() {

		val a = ('a'..'z').toMutableList()
		a.shuffle()

		val l = measureTimeMillis { a.sorted() }
		System.out.print(l)

	}

}
