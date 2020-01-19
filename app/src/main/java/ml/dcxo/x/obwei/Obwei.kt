package ml.dcxo.x.obwei

import android.app.Application
import android.os.AsyncTask
import kotlinx.coroutines.*

/**
 * Created by David on 25/10/2018 for XOXO
 */

val DispatcherAsyncTask: CoroutineDispatcher
	get() = AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()

class Obwei: Application()