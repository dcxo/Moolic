package ml.dcxo.x.obwei.viewModel.providers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.utils.makeBlacklistQuery
import ml.dcxo.x.obwei.viewModel.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by David on 16/12/2018 for XOXO
 */
object PlaylistsProvider {

	private val playlistProjection = arrayOf(
		MediaStore.Audio.Playlists._ID,
		MediaStore.Audio.Playlists.NAME,
		MediaStore.Audio.Playlists.DATE_ADDED,
		MediaStore.Audio.Playlists.DATA
	)
	private val tracksProjection = arrayOf(
		MediaStore.Audio.Media._ID,         //0
		MediaStore.Audio.Media.TITLE,       //1
		MediaStore.Audio.Media.ALBUM_ID,    //2
		MediaStore.Audio.Media.ALBUM,       //3
		MediaStore.Audio.Media.ARTIST_ID,   //4
		MediaStore.Audio.Media.ARTIST,      //5
		MediaStore.Audio.Media.YEAR,        //6
		MediaStore.Audio.Media.DURATION,    //7
		MediaStore.Audio.Media.DATE_ADDED,  //8
		MediaStore.Audio.Media.DATA,        //9
		MediaStore.Audio.Media.TRACK,       //10
		MediaStore.Audio.Playlists.Members.PLAY_ORDER
	)

	private val lastAddedProjection = arrayOf(
		MediaStore.Audio.Media._ID,         //0
		MediaStore.Audio.Media.TITLE,       //1
		MediaStore.Audio.Media.ALBUM_ID,    //2
		MediaStore.Audio.Media.ALBUM,       //3
		MediaStore.Audio.Media.ARTIST_ID,   //4
		MediaStore.Audio.Media.ARTIST,      //5
		MediaStore.Audio.Media.YEAR,        //6
		MediaStore.Audio.Media.DURATION,    //7
		MediaStore.Audio.Media.DATE_ADDED,  //8
		MediaStore.Audio.Media.DATA,        //9
		MediaStore.Audio.Media.TRACK        //10
	)

	fun getPlaylists(ctx: Context): ArrayList<Playlist> {

		val playlists = arrayListOf<Playlist>()

		getPlaylistsCursor(ctx)?.use {

			if (it.count == 0) return@use

			it.moveToFirst()
			val sqnc = sequence { do yield(it) while (it.moveToNext()) }
			for (crs in sqnc) playlists += createPlaylist(ctx, crs)

		}
		getLastSongsCursor(ctx)?.use {

			if (it.count == 0) return@use

			val tracks = Tracklist()
			it.moveToFirst()
			val sqnc = sequence { do yield(it) while (it.moveToNext()) }
			for (crs in sqnc) tracks += createSong(crs)

			playlists.add(0,
				Playlist(
					id = -1,
					name = ctx.getString(R.string.recently_added),
					modDate = Date().time,
					trackList = tracks
				)
			)

		}

		return playlists

	}

	private fun getPlaylistsCursor(ctx: Context): Cursor? =
		ctx.contentResolver.query(
			MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
			playlistProjection,
			"",
			null,
			null
		)
	private fun getPlaylistTracksCursor(ctx: Context, uri: Uri): Cursor? =
		ctx.contentResolver.query(
			uri,
			tracksProjection,
			makeBlacklistQuery(ctx),
			null,
			"${MediaStore.Audio.Playlists.Members.PLAY_ORDER} ASC"
		)
	private fun getLastSongsCursor(ctx: Context): Cursor? {

		val date = Date().time/1000
		val coerce = System.currentTimeMillis()/1000 - TimeUnit.DAYS.toSeconds(61)

		val where =
			"${makeBlacklistQuery(ctx)} and ${MediaStore.Audio.Media.DATE_ADDED} between $coerce and $date"

		return ctx.contentResolver.query(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
			lastAddedProjection,
			where,
			null,
			"${MediaStore.Audio.Media.DATE_ADDED} DESC"
		)

	}

	private fun createPlaylist(ctx: Context, cursor: Cursor): Playlist {

		val playlist = Playlist(
			id = cursor.getInt(0),
			name = cursor.getString(1),
			modDate = cursor.getLong(2),
			filePath = cursor.getString(3),
			trackList = Tracklist()
		)

		val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", cursor.getLong(0))
		getPlaylistTracksCursor(ctx, uri)?.use {

			if (it.count == 0) return@use
			val tracks = Tracklist()

			it.moveToFirst()
			val sqnc = sequence { do yield(it) while (it.moveToNext()) }
			for (crs in sqnc) {
				tracks += createSong(crs)
				Log.v("PLAY_ORDER", it.getLong(11).toString())
			}

			playlist.trackList = tracks

		}

		return playlist

	}
	private fun createSong(cursor: Cursor): Song {
		return Song(
			id = cursor.getInt(0),
			title = cursor.getString(1),
			albumId = cursor.getInt(2),
			albumTitle = cursor.getString(3),
			track = cursor.getInt(10),
			artistId = cursor.getInt(4),
			artistName = cursor.getString(5),
			year = cursor.getInt(6),
			duration = cursor.getLong(7),
			modDate = cursor.getLong(8),
			filePath = cursor.getString(9)
		)
	}

}