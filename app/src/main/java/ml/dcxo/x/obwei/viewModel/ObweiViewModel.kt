package ml.dcxo.x.obwei.viewModel

import android.app.Application
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.*
import ml.dcxo.x.obwei.Obwei
import ml.dcxo.x.obwei.viewModel.providers.*

/**
 * Created by David on 25/10/2018 for XOXO
 */
class ObweiViewModel(app: Application): AndroidViewModel(app) {

	private var songs: MutableLiveData<Tracklist> = MutableLiveData()
	private var albums: MutableLiveData<ArrayList<Album>> = MutableLiveData()
	private var artists: MutableLiveData<ArrayList<Artist>> = MutableLiveData()
	private var playlists: MutableLiveData<ArrayList<Playlist>> = MutableLiveData()

	private val contentObserver = MusicContentObserver( Handler() )

	init {
		loadEverything()
		app.contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, contentObserver)
	}

	fun loadEverything() {
		getSongs(true)
		getAlbums(true)
		getArtist(true)
		getPlaylist(true)
	}

	fun getSongs(forced: Boolean = false): LiveData<Tracklist> {

		if (songs.value.isNullOrEmpty() || forced)
			songs.postValue(SongsProvider.getSongs(getApplication()))

		return songs

	}
	fun getAlbums(forced: Boolean = false): LiveData<ArrayList<Album>> {

		if (albums.value.isNullOrEmpty() || forced)
			albums.postValue( AlbumsProvider.getAlbums(getApplication()) )

		return albums

	}
	fun getArtist(forced: Boolean = false): LiveData<ArrayList<Artist>> {

		if (artists.value.isNullOrEmpty() || forced)
			artists.postValue( ArtistsProvider.getArtists(getApplication()) )

		return artists

	}
	fun getPlaylist(forced: Boolean = false): LiveData<ArrayList<Playlist>> {

		if (playlists.value.isNullOrEmpty() || forced)
			playlists.postValue( PlaylistsProvider.getPlaylists(getApplication()) )

		return playlists

	}

	override fun onCleared() {
		super.onCleared()
		getApplication<Obwei>().contentResolver.unregisterContentObserver(contentObserver)
	}

	inner class MusicContentObserver(var handler: Handler) : ContentObserver(handler) {

		var runnable = Runnable { loadEverything() }

		override fun onChange(selfChange: Boolean) {
			onChange(selfChange, null)
		}
		override fun onChange(selfChange: Boolean, uri: Uri?) {
			handler.removeCallbacks(runnable)
			handler.post(runnable)
		}
		override fun deliverSelfNotifications(): Boolean = true

	}

}