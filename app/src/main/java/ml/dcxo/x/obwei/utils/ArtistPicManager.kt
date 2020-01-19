package ml.dcxo.x.obwei.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

fun getArtistPicURL(ctx: Context, name: String): Pair<String?, Deferred<JsonObject>?> {

	if (name.toLowerCase() == "<unknown>") return (null to null)

	val l = "${ctx.filesDir.canonicalPath}/$name.jpg"
	val f = File(l)

	return if (f.exists()) {
		Log.w("GET ARTIST PIC URL", "File used")
		(f.absolutePath to null)
	} else {
		Log.w("GET ARTIST PIC URL", "Downloaded")
		val connectivityManager = ctx.getSystemService<WifiManager>()
		if (connectivityManager?.isWifiEnabled != true || connectivityManager.connectionInfo?.networkId == -1)
			(null to null)
		else
			(null to LastFmService.service.getArtistInfoJSON(name))
	}

}