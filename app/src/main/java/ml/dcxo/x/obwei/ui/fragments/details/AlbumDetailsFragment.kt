package ml.dcxo.x.obwei.ui.fragments.details

import android.content.Context
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.AlbumTracksAdapter
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseDetailsFragment
import ml.dcxo.x.obwei.viewModel.Album
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 29/01/2019 for ObweiX
 */
class AlbumDetailsFragment: BaseDetailsFragment<Album>() {

	override val errorResource: Int = R.drawable.drawable_error_album_art_album

	override fun getAdapter(): BaseAdapter<Song, *> = AlbumTracksAdapter()
	override fun Album.getTitle(): String = title
	override fun Album.getData(): ArrayList<Song> = trackList
	override suspend fun Album.getImageUrl(ctx: Context): String = getAlbumArtURI

}