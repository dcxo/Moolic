package ml.dcxo.x.obwei.viewModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Model: Parcelable

/*
* TODO: Complete this models
* */

@Parcelize data class Song(
	var id: Int = 0, var title: String = "",
	var albumId: Int = 0, var albumTitle: String = "",
	var artistId: Int = 0, var artistName: String = "",
	var year: Int = 0, var duration: Long = 0,
	var modDate: Long = 0, var filePath: String = ""
): Model()

@Parcelize data class Album(
	var id: Int = 0, var title: String = "",
	var artistId: Int = 0, var artistName: String = "",
	var trackList: ArrayList<Song> = arrayListOf()
): Model()

@Parcelize data class Artist(
	var id: Int = 0, var name: String = "",
	var trackList: ArrayList<Song> = arrayListOf()
): Model()

@Parcelize data class Playlist(
	var name: String = "", var trackList: ArrayList<Song> = arrayListOf()
): Model()
