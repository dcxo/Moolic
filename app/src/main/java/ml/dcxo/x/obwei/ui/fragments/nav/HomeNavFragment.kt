package ml.dcxo.x.obwei.ui.fragments.nav

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_nav_home.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.PlaylistsAdapter
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.EditBlacklistDialog
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.PlaylistBottomDialog
import ml.dcxo.x.obwei.ui.dialogs.createPlaylistDialog
import ml.dcxo.x.obwei.ui.fragments.PlaylistTracksFragment
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Playlist

/**
 * Created by David on 18/12/2018 for XOXO
 */
class HomeNavFragment: BaseFragment() {

	override val layoutInflated: Int = R.layout.fragment_nav_home

	private fun updateEditBlacklistButton() {
		view?.settings?.alpha = if (Settings[context].blacklist.isEmpty()) 0.1f else 0.618f
		view?.settings?.setOnClickListener {

			if (Settings[context].blacklist.isNotEmpty())
				fragmentManager?.let { it1 ->
					EditBlacklistDialog.newInstance(this::updateEditBlacklistButton).show(it1, "Edit Blacklist")
				}

		}
	}

	override fun editOnCreateView(view: View) {
		super.editOnCreateView(view)

		view.recyclerNav.layoutManager = LinearLayoutManager(view.context)
		view.recyclerNav.adapter = PlaylistsAdapter().apply {
			click = {_, i ->
				val p = (activity as? UniqueActivity)?.obweiViewModel?.getPlaylist() ?: throw IllegalAccessError()
				val newP = Transformations.switchMap<ArrayList<Playlist>, Playlist>(p) {
					return@switchMap MutableLiveData<Playlist>().apply { value = it[i] }
				}
				val f = PlaylistTracksFragment.newInstance(newP, mActivity?.liveMiniPlayerVisibility)
				fragmentManager?.beginTransaction()
					?.setCustomAnimations(
						R.anim.slide_up,
						R.anim.slide_down,
						R.anim.slide_up,
						R.anim.slide_down
					)
					?.replace(R.id.detailsHost, f, detailsFragmentTag)
					?.commit()
			}
			longClick = {playlist, i ->
				if (playlist.id >= 0) fragmentManager?.let {
					PlaylistBottomDialog().apply {
						item = playlist
						listPos = i
						uiInteractions = this@HomeNavFragment.uiInteractions
					}.show(it, albumTrackBottomDialogFragmentTag)
				}
			}
		}

		view.headerNav.updatePadding(top = statusBarSize)

		view.settings.alpha = if (Settings[context].blacklist.isEmpty()) 0.1f else 0.618f
		view.settings.setOnClickListener {

			if (Settings[context].blacklist.isNotEmpty())
				fragmentManager?.let { it1 ->
					EditBlacklistDialog.newInstance(this::updateEditBlacklistButton).show(it1, "Edit Blacklist")
				}

		}

		view.noContentImage.setOnClickListener { createPlaylistDialog(activity ?: view.context).show() }
		view.addPlaylist.setOnClickListener { createPlaylistDialog(activity ?: view.context).show() }

	}
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		(activity as? UniqueActivity)?.obweiViewModel?.getPlaylist()?.observe(this, Observer {

			view?.noContentView?.visibility = if (it.isNotEmpty()) View.GONE else View.VISIBLE
			view?.recyclerNav?.visibility = if (it.isNotEmpty()) View.VISIBLE else View.GONE

			if (it.isNotEmpty()) (view?.recyclerNav?.adapter as PlaylistsAdapter).updateData(it)

		})

	}

}