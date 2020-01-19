package ml.dcxo.x.obwei.utils

import java.util.concurrent.TimeUnit


/**
 * Created by David on 17/11/2018 for XOXO
 */
fun millisToString(millis: Long): String {

	return "%02d:%02d".format(
		TimeUnit.MILLISECONDS.toMinutes(millis),
		TimeUnit.MILLISECONDS.toSeconds(millis) % 60
	)

}