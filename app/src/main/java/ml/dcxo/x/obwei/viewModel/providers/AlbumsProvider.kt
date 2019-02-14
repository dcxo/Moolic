package ml.dcxo.x.obwei.viewModel.providers

import android.content.Context
import android.provider.MediaStore
import ml.dcxo.x.obwei.viewModel.Album
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 30/10/2018 for XOXO
 */
object AlbumsProvider {

	private const val sortOrder =
		"${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE ASC, " +
			"${MediaStore.Audio.Media.TRACK} ASC, " +
			"${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

	fun getAlbums(context: Context): ArrayList<Album> {

		val albums = arrayListOf<Album>()

		SongsProvider.getCursor(context, sortOrder).use {

			if (it.count < 1) return@use
			it.moveToFirst()

			val sqnc = sequence { do yield(it) while (it.moveToNext()) }
			for (crs in sqnc) {
				val song = SongsProvider.createSong(crs)
				createOrGetAlbum(song, albums).trackList.add(song)
			}

		}

		return albums

	}

	private fun createOrGetAlbum(song: Song, albums: ArrayList<Album>): Album {

		return try {
			val album = albums.first { it.title == song.albumTitle }
			album
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