package ml.dcxo.x.obwei.base

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.UniqueActivity

/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseFragment: Fragment() {

	@get:LayoutRes abstract val layoutInflated: Int

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val v = inflater.inflate(layoutInflated, container, false)
		editOnCreateView(v)
		return v
	}

	open fun editOnCreateView(view: View) {}

}