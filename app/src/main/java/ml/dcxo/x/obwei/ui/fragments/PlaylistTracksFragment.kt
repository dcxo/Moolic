package ml.dcxo.x.obwei.ui.fragments

import android.opengl.Visibility
import android.os.Build
import android.view.View
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_playlist_tracks.view.*
import ml.dcxo.x.obwei.adapters.PlaylistTracksAdapter
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.DetailsTrackBottomDialog
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 29/12/2018 for XOXO
 */
class PlaylistTracksFragment: BaseFragment() {

	lateinit var playlist: LiveData<Playlist>

	var liveMiniPlayerVisibility: LiveData<Boolean>? = null

	override val layoutInflated: Int = R.layout.fragment_playlist_tracks

	private fun onClickItem(song: Song, position: Int) {
		uiInteractions?.onSongSelected(song, playlist.value?.trackList ?: Tracklist(), position)
	}
	private fun onLongClickItem(song: Song, position: Int) {
		uiInteractions?.let {
			val d = DetailsTrackBottomDialog.newInstance(
				song, { i -> view?.detailsRv?.adapter?.notifyItemRemoved(i)},
				it, position
			)
			fragmentManager?.let { it1 -> d.show(it1, songBottomDialogFragmentTag) }
		}
	}

	override fun editOnCreateView(view: View) {
		super.editOnCreateView(view)

		view.setOnClickListener {}
		view.title.updatePadding(top = statusBarSize + 8.dp)

		val adapter = PlaylistTracksAdapter().apply {
			click = this@PlaylistTracksFragment::onClickItem
			longClick = this@PlaylistTracksFragment::onLongClickItem
		}
		val manager = LinearLayoutManager(view.context)

		playlist.observe(this, Observer {
			this.view?.title?.text = it.name
			(this.view?.detailsRv?.adapter as? PlaylistTracksAdapter)?.updateData( it.trackList )
		})
		liveMiniPlayerVisibility?.observe(this, Observer {
			this.view?.detailsRv?.updatePadding(bottom = if (it) 35.dp else 0)
		})

		view.detailsRv.apply {
			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					this@PlaylistTracksFragment.view?.title?.isSelected =
							recyclerView.canScrollVertically(-1)
				}
			})
			this.adapter = adapter
			this.layoutManager = manager

		}

	}
	override fun onDestroyView() {

		view?.detailsRv?.clearOnScrollListeners()

		super.onDestroyView()
	}

	companion object {
		fun newInstance(playlist: LiveData<Playlist>, liveMiniPlayerVisibility: LiveData<Boolean>?): PlaylistTracksFragment =
			PlaylistTracksFragment().apply {
				this.playlist = playlist
				this.liveMiniPlayerVisibility = liveMiniPlayerVisibility
			}
	}

}