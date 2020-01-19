package ml.dcxo.x.obwei.ui.fragments.nav

import android.content.Context
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.adapters.ArtistsAdapter
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.ArtistBottomDialog
import ml.dcxo.x.obwei.utils.MarginDecor
import ml.dcxo.x.obwei.utils.artistBottomDialogFragmentTag
import ml.dcxo.x.obwei.viewModel.Artist
import ml.dcxo.x.obwei.R

/**
 * Created by David on 02/11/2018 for XOXO
 */
class ArtistsNavFragment: BaseNavFragment<Artist, ArtistsAdapter>() {

	override val decor: RecyclerView.ItemDecoration? = MarginDecor()
	override val click: ((Artist, Int) -> Unit)? = {artist, i ->
		mActivity?.hideKeyboard()

		val l = Transformations.switchMap(getLiveData()) {
			return@switchMap MutableLiveData<Artist>().apply {
				value = it.firstOrNull { artist1 -> artist1.id == artist.id }
			}
		}
		mActivity?.showArtist(l)
	}
	override val longClick: ((Artist, Int) -> Unit)? = {artist, i ->
		mActivity?.hideKeyboard()

		fragmentManager?.let {
			ArtistBottomDialog.newInstance(artist, this, i)
				.show(it, artistBottomDialogFragmentTag)
		}
	}
	override val noContentMessage: Int = R.string.no_artists_found
	override val noContentImage: Int = R.drawable.icon_person

	override fun filterData(model: Artist, query: String): Boolean = model.name.contains(query, true)
	override fun getToolbarTitle(): String = "${getLiveData().value?.size ?: "No"} Artists"
	override fun getLiveData(): LiveData<ArrayList<Artist>> =
		mActivity?.obweiViewModel?.getArtist() ?: MutableLiveData()
	override fun getAdapterAndLayoutManager(context: Context): Pair<ArtistsAdapter, RecyclerView.LayoutManager> =
		ArtistsAdapter() to GridLayoutManager(context, 2)

}