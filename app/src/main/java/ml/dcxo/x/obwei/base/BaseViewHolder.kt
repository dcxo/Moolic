package ml.dcxo.x.obwei.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.viewModel.Model
import java.text.FieldPosition


/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseViewHolder<Item: Model>(itemView: View): RecyclerView.ViewHolder(itemView) {

	open fun bind(i: Item, position: Int, click: ((Item, Int)->Unit)?, longClick: ((Item, Int)->Unit)?) {
		itemView.setOnClickListener { click?.invoke(i, position) }
		itemView.setOnLongClickListener { longClick?.invoke(i, position); true }
	}
	abstract fun recycle()

}