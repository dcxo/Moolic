package ml.dcxo.x.obwei.viewModel

import android.content.ContentUris
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

sealed class Model: Parcelable

/*
* TODO: Complete this models
* */

@Parcelize data class Song(
	var id: Int = 0, var title: String = "",
	var albumId: Int = 0, var albumTitle: String = "", var track: Int = 0,
	var artistId: Int = 0, var artistName: String = "",
	var year: Int = 0, var duration: Long = 0,
	var modDate: Long = 0, var filePath: String = ""
): Model() {
	@IgnoredOnParcel val getAlbumArtURI: String; get() = ContentUris.withAppendedId(
			Uri.parse("content://media/external/audio/albumart"),
			albumId.toLong()).toString()

	companion object {
		val NULL = Song()
	}

}
typealias Tracklist = ArrayList<Song>

@Parcelize data class Album(
	var id: Int = 0, var title: String = "",
	var artistId: Int = 0, var artistName: String = "",
	var trackList: Tracklist = arrayListOf()
): Model() {
	@IgnoredOnParcel val getAlbumArtURI: String; get() = ContentUris.withAppendedId(
		Uri.parse("content://media/external/audio/albumart"),
		id.toLong()).toString()
}

@Parcelize data class Artist(
	var id: Int = 0, var name: String = "",
	var trackList: Tracklist = arrayListOf()
): Model()

@Parcelize data class Playlist(
	var id: Int = 0, var name: String = "",
	var filePath: String = "", var modDate: Long = 0,
	var trackList: Tracklist = arrayListOf()
): Model()
