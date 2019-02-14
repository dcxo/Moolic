package ml.dcxo.x.obwei.base

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ml.dcxo.x.obwei.viewModel.Model
import kotlin.coroutines.CoroutineContext


/**
 * Created by David on 02/11/2018 for XOXO
 */
abstract class BaseViewHolder<Item: Model>(itemView: View):
	RecyclerView.ViewHolder(itemView), LayoutContainer, CoroutineScope {

	override val coroutineContext: CoroutineContext = Dispatchers.Main
	override val containerView: View? = itemView

	fun realBind(i: Item, position: Int, click: ((Item, Int)->Unit)?, longClick: ((Item, Int)->Unit)?) {
		containerView?.setOnClickListener { click?.invoke(i, position) }
		containerView?.setOnLongClickListener { longClick?.invoke(i, position); true }

		bind(i, position)

	}

	abstract fun bind(i: Item, position: Int)
	open fun changeViewWithPayloads(changes: Bundle) {}
	open fun recycle() {}

}