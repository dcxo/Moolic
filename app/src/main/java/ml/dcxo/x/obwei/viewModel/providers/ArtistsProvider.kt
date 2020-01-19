package ml.dcxo.x.obwei.viewModel.providers

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import ml.dcxo.x.obwei.utils.makeBlacklistQuery
import ml.dcxo.x.obwei.viewModel.Artist
import ml.dcxo.x.obwei.viewModel.Song
import kotlin.system.measureTimeMillis

/**
 * Created by David on 30/10/2018 for XOXO
 */
object ArtistsProvider {

	private const val sortOrder =
		"${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE ASC, " +
				"${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE ASC, " +
				"${MediaStore.Audio.Media.TRACK} ASC, " +
				"${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

	fun getArtists(context: Context): ArrayList<Artist> {


		val artists = arrayListOf<Artist>()

		SongsProvider.getCursor(context, sortOrder).use {

			if (it.count < 1) return@use
			it.moveToFirst()

			val sqnc = sequence { do yield(it) while (it.moveToNext()) }
			for (crs in sqnc) {
				val song = SongsProvider.createSong(crs)
				ArtistsProvider.createOrGetArtist(song, artists).trackList.add(song)
			}

		}

		return artists

	}

	private fun createOrGetArtist(song: Song, artists: ArrayList<Artist>): Artist {

		val name = song.artistName.split(", ", " feat", " ft", " x ", ignoreCase = true).first().trim('(', ')')

		artists.any { if (name.toLowerCase() == it.name.toLowerCase()) return it; false }

		val artist = Artist(id = song.artistId, name = name)
		artists += artist

		return artist

	}

}