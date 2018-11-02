package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.adapters.ArtistsAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.utils.MarginDecor
import ml.dcxo.x.obwei.viewModel.Artist

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class ArtistsNavFragment: BaseNavFragment<Artist, ArtistsAdapter>() {

	override val decor: RecyclerView.ItemDecoration? = MarginDecor()

	override fun getLiveData(): LiveData<ArrayList<Artist>> =
		(activity as UniqueActivity).obweiViewModel.getArtist()

	override fun getAdapterAndLayoutManager(context: Context): Pair<ArtistsAdapter, RecyclerView.LayoutManager> =
		ArtistsAdapter() to GridLayoutManager(context, 2)

}