package ml.dcxo.x.obwei.viewModel.providers

import ml.dcxo.x.obwei.viewModel.Artist
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 30/10/2018 for ObweiX
 */
object ArtistsProvider {

	fun getArtists(songs: ArrayList<Song>): ArrayList<Artist> {

		val artists = arrayListOf<Artist>()

		songs.forEach { createOrGetArtist(it, artists).trackList.add(it) }
		//artists.sortBy { it.name }

		return artists

	}

	private fun createOrGetArtist(song: Song, artists: ArrayList<Artist>): Artist {

		val name = song.artistName.split(",", "feat", "ft", "x", ignoreCase = true).first().trim('(', ' ', ')')

		return try {
			artists.first { name == it.name }
		} catch (e: NoSuchElementException) {
			val artist = Artist(
				id = song.artistId, name = name
			)
			artists += artist
			return artist
		}

	}

}