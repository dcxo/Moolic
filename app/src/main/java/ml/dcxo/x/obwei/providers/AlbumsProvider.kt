package ml.dcxo.x.obwei.providers

import android.content.Context
import ml.dcxo.x.obwei.Album
import ml.dcxo.x.obwei.Song
import kotlin.contracts.contract

/**
 * Created by David on 30/10/2018 for ObweiX
 */
object AlbumsProvider {

	fun getAlbums(songs: ArrayList<Song>): ArrayList<Album> {

		val albums = arrayListOf<Album>()

		songs.forEach { createOrGetAlbum(it, albums).trackList.add(it) }

		return albums

	}

	private fun createOrGetAlbum(song: Song, albums: ArrayList<Album>): Album {

		return try {
			albums.first { it.title == song.albumTitle }
		} catch (e: NoSuchElementException) {
			val album = Album(
				id = song.albumId, title = song.albumTitle,
				artistId = song.artistId, artistName = song.artistName
			)
			albums += album
			album
		}

	}

}