package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.adapters.SongsAdapter
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class SongsNavFragment: BaseNavFragment<Song, SongsAdapter>() {

	override fun getLiveData(): LiveData<ArrayList<Song>> =
		(activity as UniqueActivity).obweiViewModel.getSongs()

	override fun getAdapterAndLayoutManager(context: Context): Pair<SongsAdapter, RecyclerView.LayoutManager> =
		SongsAdapter() to LinearLayoutManager(context)

}