package ml.dcxo.x.obwei.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.nav_fragment.*
import kotlinx.android.synthetic.main.nav_fragment.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.getStatusBarHeight
import ml.dcxo.x.obwei.viewModel.Model

/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseNavFragment<Item: Model, Adapter: BaseAdapter<Item, *>>: BaseFragment() {

	override val layoutInflated: Int = R.layout.nav_fragment

	open val decor: RecyclerView.ItemDecoration? = null

	fun View.getAdapter(): Adapter = this.recyclerNav.adapter as Adapter

	override fun editOnCreateView(view: View) {

		val (adapter, lManager) = getAdapterAndLayoutManager(view.context)

		view.recyclerNav.adapter = adapter
		view.recyclerNav.layoutManager = lManager
		view.recyclerNav.updatePadding(top = getStatusBarHeight)

		for (i in 0 until view.recyclerNav.itemDecorationCount) view.recyclerNav.removeItemDecorationAt(i)
		decor?.let { view.recyclerNav.addItemDecoration(it) }

	}
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		getLiveData().observe(this, Observer {
			view?.getAdapter()?.updateData(it)
		})

	}

	abstract fun getLiveData(): LiveData<ArrayList<Item>>
	abstract fun getAdapterAndLayoutManager(context: Context):
			Pair<Adapter, RecyclerView.LayoutManager>

}