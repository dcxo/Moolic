package ml.dcxo.x.obwei.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_nav.*
import kotlinx.android.synthetic.main.fragment_nav.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.utils.dp
import ml.dcxo.x.obwei.utils.statusBarSize
import ml.dcxo.x.obwei.viewModel.Model
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 02/11/2018 for XOXO
 */
abstract class BaseNavFragment<Item: Model, Adapter: BaseAdapter<Item, *>>: BaseFragment(),
	SearchView.OnQueryTextListener {

	val notifyRemove: NotifyRemoveFun = { i: Int ->
		view?.getAdapter()?.notifyItemRemoved(i)
	}

	override val layoutInflated: Int = R.layout.fragment_nav

	open val decor: RecyclerView.ItemDecoration? = null
	open val click: ((Item, Int)->Unit)? = null
	open val longClick: ((Item, Int)->Unit)? = null
	open val touchHelper: ItemTouchHelper? = null

	fun View.getAdapter(): Adapter = this.recyclerNav.adapter as Adapter

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		mActivity?.liveMiniPlayerVisibility?.observe(this, Observer {
			recyclerNav.updateLayoutParams<CoordinatorLayout.LayoutParams> {
				updateMargins(bottom = if (it) 35.dp else 0.dp )
			}
			fabNav.updateLayoutParams<CoordinatorLayout.LayoutParams> {
				updateMargins(bottom = if (it) 51.dp else 16.dp )
			}
		})
		getLiveData().observe(this, Observer {
			view?.getAdapter()?.updateData(it)
			view?.toolbar?.title = getToolbarTitle()
		})

	}
	override fun editOnCreateView(view: View) {

		val (adapter, lManager) = getAdapterAndLayoutManager(view.context)

		view.toolbar.updatePadding(top = statusBarSize)
		view.toolbar.inflateMenu(R.menu.search_menu)
		view.toolbar.title = getToolbarTitle()
		(view.toolbar.menu.findItem(R.id.searchOption).actionView as SearchView).setOnQueryTextListener(this)

		view.recyclerNav.adapter = adapter.apply {
			click = this@BaseNavFragment.click
			longClick = this@BaseNavFragment.longClick
		}
		view.recyclerNav.layoutManager = lManager

		view.fabNav.hide()
		view.fabNav.setOnClickListener {
			val value = (activity as? UniqueActivity)?.obweiViewModel?.getSongs()?.value ?: Tracklist()
			if (value.size > 0) {
				uiInteractions?.onShuffleToggle(true)
				uiInteractions?.onShuffleAllButtonClicked(value)
			}
		}

		for (i in 0 until view.recyclerNav.itemDecorationCount) view.recyclerNav.removeItemDecorationAt(i)
		decor?.let { view.recyclerNav.addItemDecoration(it) }

		touchHelper?.attachToRecyclerView(view.recyclerNav)

	}

	override fun onDestroyView() {

		view?.fabNav?.setOnClickListener(null)
		view?.recyclerNav?.clearOnScrollListeners()

		super.onDestroyView()
	}

	override fun onQueryTextChange(newText: String?): Boolean {

		val query = newText ?: ""

		val data = ArrayList(getLiveData().value ?: arrayListOf())
		val filteredData = ArrayList( data.filter { filterData(it, query) } )
		view?.getAdapter()?.updateData(filteredData)

		return query.isNotBlank()

	}
	override fun onQueryTextSubmit(query: String?): Boolean = false

	abstract fun filterData(model: Item, query: String): Boolean
	abstract fun getToolbarTitle(): String
	abstract fun getLiveData(): LiveData<ArrayList<Item>>
	abstract fun getAdapterAndLayoutManager(context: Context): Pair<Adapter, RecyclerView.LayoutManager>

}