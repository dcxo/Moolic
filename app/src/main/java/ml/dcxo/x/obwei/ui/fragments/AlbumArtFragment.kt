package ml.dcxo.x.obwei.ui.fragments

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_albumart.view.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 13/11/2018 for XOXO
 */
class AlbumArtFragment: BaseFragment() {

	var song: Song? = null
	var ambiColor: AmbiColor = AmbiColor.NULL

	override val layoutInflated: Int = R.layout.fragment_albumart

	override fun editOnCreateView(view: View) {

		Glide.with(view.context).asBitmap().load(song?.getAlbumArtURI)
			.apply(RequestOptions.centerCropTransform())
			.error(Glide.with(view).asBitmap().load(R.drawable.drawable_error_album_art_song))
			.into(view.albumArt)

	}

	companion object {

		fun newInstance(song: Song): AlbumArtFragment {
			return AlbumArtFragment().apply { this.song = song }
		}

	}

}