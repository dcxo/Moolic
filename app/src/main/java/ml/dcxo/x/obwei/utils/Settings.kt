package ml.dcxo.x.obwei.utils

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.edit

/**
 * Created by David on 17/11/2018 for XOXO
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class Settings(context: Context) {

	var preferences: SharedPreferences = context.getSharedPreferences("X_OBWEI_SETTINGS_X", 0)

	var blacklist: Set<String>
		get() = preferences.getStringSet(blacklistKey, setOf())
		set(value) = preferences.edit { putStringSet(blacklistKey, value) }

	@PlaybackStateCompat.RepeatMode var repeatMode: Int
		get() = preferences.getInt(repeatKey, PlaybackStateCompat.REPEAT_MODE_NONE)
		set(value) = preferences.edit { putInt(repeatKey, value) }
	var shuffleMode: Boolean
		get() = preferences.getBoolean(shuffleKey, false)
		set(value) = preferences.edit { putBoolean(shuffleKey, value) }

	fun addToBlacklist(ss: List<String>) {
		blacklist += ss
	}


	companion object {

		var INSTANCE: Settings? = null
		operator fun get(context: Context?): Settings {
			if (INSTANCE == null)
				INSTANCE = Settings(context!!)
			return INSTANCE!!
		}

	}

}