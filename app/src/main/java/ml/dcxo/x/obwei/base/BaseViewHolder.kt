package ml.dcxo.x.obwei.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.viewModel.Model


/**
 * Created by David on 02/11/2018 for ObweiX
 */
abstract class BaseViewHolder<Item: Model>(itemView: View): RecyclerView.ViewHolder(itemView) {

	abstract fun bind(i: Item)
	abstract fun recycle()

}