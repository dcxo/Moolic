package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.adapters.AlbumsAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.utils.MarginDecor
import ml.dcxo.x.obwei.viewModel.Album

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class AlbumsNavFragment: BaseNavFragment<Album, AlbumsAdapter>() {

	override val decor: RecyclerView.ItemDecoration? = MarginDecor()

	override fun getLiveData(): LiveData<ArrayList<Album>> = (activity as UniqueActivity).obweiViewModel.getAlbums()

	override fun getAdapterAndLayoutManager(context: Context): Pair<AlbumsAdapter, RecyclerView.LayoutManager> =
			AlbumsAdapter() to GridLayoutManager(context, 2)

}