package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.adapters.AlbumsAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.AlbumBottomDialog
import ml.dcxo.x.obwei.utils.MarginDecor
import ml.dcxo.x.obwei.utils.albumBottomDialogFragmentTag
import ml.dcxo.x.obwei.viewModel.Album

/**
 * Created by David on 02/11/2018 for XOXO
 */
class AlbumsNavFragment: BaseNavFragment<Album, AlbumsAdapter>() {

	override val click: ((Album, Int) -> Unit)? = { album, _ ->
		mActivity?.hideKeyboard()

		val l = Transformations.switchMap(getLiveData()) {
			val forLiveAlbum = it.firstOrNull { listAlbum -> listAlbum.id == album.id }
			return@switchMap MutableLiveData<Album>().apply { value = forLiveAlbum }
		}
		(activity as? UniqueActivity)?.showAlbum(l)
	}
	override val longClick: ((Album, Int) -> Unit)? = { album, i ->
		mActivity?.hideKeyboard()

		fragmentManager?.let {
			AlbumBottomDialog
				.newInstance(album, this, i)
				.show(it, albumBottomDialogFragmentTag)

		}
	}
	override val decor: RecyclerView.ItemDecoration? = MarginDecor()

	override fun getLiveData(): LiveData<ArrayList<Album>> =
		mActivity?.obweiViewModel?.getAlbums() ?: MutableLiveData()
	override fun getToolbarTitle(): String = "${getLiveData().value?.size ?: "No"} Albums"
	override fun filterData(model: Album, query: String): Boolean =
		model.title.contains(query, true)
	override fun getAdapterAndLayoutManager(context: Context): Pair<AlbumsAdapter, RecyclerView.LayoutManager> =
		AlbumsAdapter() to GridLayoutManager(context, 2)


}