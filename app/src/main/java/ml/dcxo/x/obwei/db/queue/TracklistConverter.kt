package ml.dcxo.x.obwei.db.queue

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 23/02/2019 for ObweiX
 */
class TracklistConverter {

	@TypeConverter fun stringToTracklist(string: String): Tracklist {
		return GsonBuilder().create().fromJson(string, object:TypeToken<Tracklist>(){}.type)
	}

	@TypeConverter fun tracklistToString(tracklist: Tracklist): String {
		return GsonBuilder().create().toJson(tracklist)
	}

}