package ml.dcxo.x.obwei.utils

import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Created by David on 09/02/2019 for ObweiX
 */

const val apiKey = "api_key=2378ae57f0ecb7847b7f11e647d88a02"
const val url = "https://ws.audioscrobbler.com/"

object LastFmService {

	private val client = OkHttpClient.Builder()
		.connectTimeout(2, TimeUnit.MINUTES)
		.readTimeout(2, TimeUnit.MINUTES)
		.writeTimeout(2, TimeUnit.MINUTES)
		.build()

	val service: LastFmApi = Retrofit.Builder()
		.baseUrl(url)
		.client(client)
		.addCallAdapterFactory(CoroutineCallAdapterFactory())
		.addConverterFactory(GsonConverterFactory.create())
		.build().create(LastFmApi::class.java)

}

interface LastFmApi {

	@GET("2.0/?method=artist.getinfo&$apiKey&format=json")
	fun getArtistInfoJSON(@Query("artist") artist: String): Deferred<JsonObject>

	@GET("2.0/?method=album.getinfo&$apiKey&format=json")
	fun getAlbumInfoJSON(
		@Query("album") album: String,
		@Query("artist") artist: String
	): Deferred<JsonObject>

}