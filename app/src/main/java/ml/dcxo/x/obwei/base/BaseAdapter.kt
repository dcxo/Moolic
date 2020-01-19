package ml.dcxo.x.obwei.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import ml.dcxo.x.obwei.viewModel.Model
import kotlin.properties.Delegates

/**
 * Created by David on 02/11/2018 for XOXO
 */
abstract class BaseAdapter<Item: Model, VH: BaseViewHolder<Item>>:
	RecyclerView.Adapter<VH>(), FastScrollRecyclerView.SectionedAdapter {

	var click: ((Item, Int)->Unit)? = null
	var longClick: ((Item, Int)->Unit)? = null

	var data: ArrayList<Item> by Delegates.observable(arrayListOf()) {_, _, _ ->
		notifyDataSetChanged()
	}

	fun updateData(newData: ArrayList<Item>) {

		DiffUtil.calculateDiff( Ziff(newData) ).dispatchUpdatesTo(this)
		data.clear()
		data.addAll(newData)

	}

	abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
	abstract fun areItemTheSame(oldItem: Item, newItem: Item): Boolean
	abstract fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean
	open fun getChanges(oldItem: Item, newItem: Item): Bundle? = null

	override fun getItemCount(): Int = data.size
	override fun onBindViewHolder(holder: VH, position: Int) = holder.realBind(data[position], position, click, longClick)
	override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {

		if (payloads.isNotEmpty()) {
			holder.changeViewWithPayloads(payloads[0] as Bundle)
		} else onBindViewHolder(holder, position)

	}
	override fun onViewRecycled(holder: VH) = holder.recycle()

	override fun getSectionName(position: Int): String = ""

	private inner class Ziff(private val newData: ArrayList<Item>): DiffUtil.Callback() {
		override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
			areItemTheSame(data[oldItemPosition], newData[newItemPosition])

		override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
			areContentsTheSame(data[oldItemPosition], newData[newItemPosition])

		override fun getOldListSize(): Int = data.size
		override fun getNewListSize(): Int = newData.size

		override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any?  =
			getChanges(data[oldItemPosition], newData[newItemPosition])

	}

}