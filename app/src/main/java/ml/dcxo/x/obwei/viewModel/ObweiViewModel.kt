package ml.dcxo.x.obwei.viewModel

import android.app.Application
import androidx.lifecycle.*
import ml.dcxo.x.obwei.viewModel.providers.*

/**
 * Created by David on 25/10/2018 for ObweiX
 */
class ObweiViewModel(app: Application): AndroidViewModel(app) {

	var songs: MutableLiveData<ArrayList<Song>> = MutableLiveData()
	var albums: MutableLiveData<ArrayList<Album>> = MutableLiveData()
	var artists: MutableLiveData<ArrayList<Artist>> = MutableLiveData()
	var playlists: MutableLiveData<ArrayList<Playlist>> = MutableLiveData()

	init {
		getSongs(true)
		getAlbums(true)
		getArtist(true)
	}

	fun getSongs(forced: Boolean = false): LiveData<ArrayList<Song>> {

		if (songs.value.isNullOrEmpty() || forced)
			songs.postValue(SongsProvider.getSongs(getApplication()))

		return songs

	}
	fun getAlbums(forced: Boolean = false): LiveData<ArrayList<Album>> {

		if (albums.value.isNullOrEmpty() || forced)
			albums.postValue( AlbumsProvider.getAlbums(songs.value ?: arrayListOf()) )

		return albums

	}
	fun getArtist(forced: Boolean = false): LiveData<ArrayList<Artist>> {

		if (artists.value.isNullOrEmpty() || forced)
			artists.postValue( ArtistsProvider.getArtists(songs.value ?: arrayListOf()) )

		return artists

	}
	//TODO: GET PLAYLIST

	// TODO: Create Content Observer

}