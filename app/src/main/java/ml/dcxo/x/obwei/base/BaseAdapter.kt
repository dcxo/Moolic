package ml.dcxo.x.obwei.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.viewModel.Model
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseAdapter<Item: Model, VH: BaseViewHolder<Item>>: RecyclerView.Adapter<VH>() {

	val data: ArrayList<Item> by Delegates.observable(arrayListOf()) {_, _, _ ->
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

	override fun getItemCount(): Int = data.size
	override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])
	override fun onViewRecycled(holder: VH) = holder.recycle()

	private inner class Ziff(private val newData: ArrayList<Item>): DiffUtil.Callback() {
		override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
			areItemTheSame(data[oldItemPosition], newData[newItemPosition])

		override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
			areContentsTheSame(data[oldItemPosition], newData[newItemPosition])

		override fun getOldListSize(): Int = data.size
		override fun getNewListSize(): Int = newData.size

	}

}