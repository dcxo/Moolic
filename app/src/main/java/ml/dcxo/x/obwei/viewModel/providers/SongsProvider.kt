package ml.dcxo.x.obwei.viewModel.providers

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import ml.dcxo.x.obwei.utils.Settings
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 30/10/2018 for ObweiX
 */
object SongsProvider {

	private val projection = arrayOf(
		MediaStore.Audio.Media._ID,         //0
		MediaStore.Audio.Media.TITLE,       //1
		MediaStore.Audio.Media.ALBUM_ID,    //2
		MediaStore.Audio.Media.ALBUM,       //3
		MediaStore.Audio.Media.ARTIST_ID,   //4
		MediaStore.Audio.Media.ARTIST,      //5
		MediaStore.Audio.Media.YEAR,        //6
		MediaStore.Audio.Media.DURATION,    //7
		MediaStore.Audio.Media.DATE_ADDED,  //8
		MediaStore.Audio.Media.DATA         //9
	)
	private const val selection = "${MediaStore.Audio.Media.IS_MUSIC}=1"
	private const val order = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

	private fun getCursor(ctx: Context, sortOrder: String): Cursor? {
		return ctx.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
			projection, makeBlacklist(ctx), null, sortOrder)
	}

	fun getSongs(ctx: Context, sortOrder: String = order): Tracklist {
		val songs = arrayListOf<Song>()

		getCursor(ctx, sortOrder)?.use {
			val sequence = sequence { do yield(it) while ( it.moveToNext() ) }
			if ( it.moveToFirst() ) for (cursor in sequence) songs += createSong(cursor)

		}

		return songs
	}

	private fun createSong(cursor: Cursor): Song {

		return Song(
			id = cursor.getInt(0),
			title = cursor.getString(1),
			albumId = cursor.getInt(2),
			albumTitle = cursor.getString(3),
			artistId = cursor.getInt(4),
			artistName = cursor.getString(5),
			year = cursor.getInt(6),
			duration = cursor.getLong(7),
			modDate = cursor.getLong(8),
			filePath = cursor.getString(9)
		)

	}

	private fun makeBlacklist(context: Context): String {

		val bl = Settings.get(context).blacklist
		val blMap = bl.groupBy { it.endsWith("%") }

		return buildString {

			append(selection)
			if (bl.isEmpty()) return toString()

			if (blMap[true] != null && blMap[true]?.isNotEmpty() == true)
				append(" AND", " LIKE (${ blMap[true]?.joinToString(",") })")
			if (blMap[false] != null && blMap[false]?.isNotEmpty() == true)
				append(" AND", " IS (${ blMap[false]?.joinToString(",") })")

		}

	}

}