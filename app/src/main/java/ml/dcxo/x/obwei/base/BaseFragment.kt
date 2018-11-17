package ml.dcxo.x.obwei.base

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.ui.UniqueActivity
import kotlin.coroutines.CoroutineContext

/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseFragment: Fragment(), CoroutineScope {

	private val job = Job()
	val uiInteractions; get() = (activity as UniqueActivity).uiInteractions
	override val coroutineContext: CoroutineContext = Dispatchers.Main + job

	@get:LayoutRes abstract val layoutInflated: Int

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val v = inflater.inflate(layoutInflated, container, false)
		editOnCreateView(v)
		return v
	}

	protected open fun editOnCreateView(view: View) {}

}