package ml.dcxo.x.obwei

import android.app.Application
import androidx.lifecycle.*

/**
 * Created by David on 25/10/2018 for ObweiX
 */
class ObweiViewModel(app: Application): AndroidViewModel(app) {

	var songs: LiveData<ArrayList<Song>> = MutableLiveData()
	var albums: LiveData<ArrayList<Album>> = MutableLiveData()
	var artists: LiveData<ArrayList<Artist>> = MutableLiveData()
	var playlists: LiveData<ArrayList<Playlist>> = MutableLiveData()

	//TODO: Make _init_

	//TODO: Add Get Methods

	// TODO: Create Content Observer

}