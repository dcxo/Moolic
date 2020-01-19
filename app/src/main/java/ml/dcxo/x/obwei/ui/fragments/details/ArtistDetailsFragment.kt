package ml.dcxo.x.obwei.ui.fragments.details

import android.content.Context
import android.graphics.Bitmap
import ml.dcxo.x.obwei.base.BaseDetailsFragment
import ml.dcxo.x.obwei.utils.getArtistPicURL
import ml.dcxo.x.obwei.viewModel.Artist
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.ArtistTracksAdapter
import ml.dcxo.x.obwei.base.BaseAdapter

/**
 * Created by David on 29/01/2019 for ObweiX
 */
class ArtistDetailsFragment: BaseDetailsFragment<Artist>() {

	override val errorResource: Int = R.drawable.drawable_error_album_art_artist

	override fun getAdapter(): BaseAdapter<Song, *> = ArtistTracksAdapter()
	override fun Artist.getData(): ArrayList<Song> = trackList
	override suspend fun Artist.getImageUrl(ctx: Context): String {
		val artistPicURL = getArtistPicURL(ctx, name)
		return artistPicURL.first ?: artistPicURL.second?.await()
			?.getAsJsonObject("artist")
			?.getAsJsonArray("image")?.get(3)
			?.asJsonObject?.get("#text")?.asString ?: ""
	}
	override fun Artist.getTitle(): String = name

	override fun addToListener(item: Artist, resource: Bitmap?, model: String) {

		resource?.let {
			if (model.startsWith("http", true))
				activity?.openFileOutput("${item.name}.jpg", Context.MODE_PRIVATE)?.use { fos ->
					it.compress(Bitmap.CompressFormat.JPEG, 100, fos)
				}
		}

	}

}