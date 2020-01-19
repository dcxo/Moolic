package ml.dcxo.x.obwei.ui.fragments

import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.request.target.ImageViewTarget
import kotlinx.android.synthetic.main.fragment_albumart.view.*
import kotlinx.coroutines.Deferred
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 13/11/2018 for XOXO
 */
class AlbumArtFragment: BaseFragment() {

	var song: Song? = null
	var ambiColor: Deferred<AmbiColor>? = null

	override val layoutInflated: Int = R.layout.fragment_albumart

	override fun editOnCreateView(view: View) {

		GlideApp.with(view.albumArt)
			.asAmbitmap()
			.load(song?.getAlbumArtURI)
			.error(R.drawable.drawable_error_album_art_song)
			.centerCrop()
			.into(object: ImageViewTarget<Ambitmap>(view.albumArt) {
				override fun setResource(resource: Ambitmap?) {
					ambiColor = resource?.ambiColor
					view.albumArt.setImageBitmap(resource?.bitmap)
				}
				override fun onLoadFailed(errorDrawable: Drawable?) {
					super.onLoadFailed(errorDrawable)
					view.albumArt.setImageDrawable(errorDrawable)
				}
			})

	}
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		val p = Point()
		view?.display?.getSize(p)

		view?.cardAlbumArt?.updateLayoutParams<ConstraintLayout.LayoutParams> {
			matchConstraintPercentWidth = 0.618f + 98.dp.toFloat() / p.x.toFloat()
		}

	}

	companion object {

		fun newInstance(song: Song): AlbumArtFragment {
			return AlbumArtFragment().apply { this.song = song }
		}

	}

}