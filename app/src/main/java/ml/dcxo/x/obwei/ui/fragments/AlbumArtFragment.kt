package ml.dcxo.x.obwei.ui.fragments

import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_albumart.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 13/11/2018 for ObweiX
 */
class AlbumArtFragment(): BaseFragment() {

	lateinit var song: Song

	override val layoutInflated: Int = R.layout.fragment_albumart

	override fun editOnCreateView(view: View) {

		Glide.with(view.context).load(song.getAlbumArtURI).asBitmap()
			.into(view.albumArt)

	}

	companion object {

		fun newInstance(song: Song): AlbumArtFragment {
			return AlbumArtFragment().apply { this.song = song }
		}

	}

}