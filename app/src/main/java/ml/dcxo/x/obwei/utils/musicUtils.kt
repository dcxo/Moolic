package ml.dcxo.x.obwei.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.contentValuesOf
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist
import java.io.File

fun addToBlacklist(context: Context?, filePaths: List<String>) {

	Settings[context].addToBlacklist(filePaths)
	/*context?.contentResolver?.notifyChange(
		MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
	)*/

}
fun removeFromDisk(context: Context?, songs: List<Song>): Boolean {

	var removed = true

	GlobalScope.launch {

		val where = buildString {
			append("${MediaStore.Audio.Media._ID} in (")
			songs.forEachIndexed { index, song ->
				append(song.id)
				if (index < songs.size - 1) append(",")
			}
			append(")")
		}

		val removeColumns = withContext(Dispatchers.Default) {
			context?.contentResolver?.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null)
		}
		if (removeColumns != songs.size) removed = false

		var removeCount = 0
		songs.forEach { song ->
			val notRemoved = withContext(Dispatchers.IO) { !File(song.filePath).delete() }
			if (notRemoved) {
				//Toast.makeText(context, "%url no sa podio borrar", Toast.LENGTH_SHORT).show()
			} else removeCount++
		}
		if (removeCount != songs.size) removed = false

	}
	return removed

}
fun makeBlacklistQuery(context: Context): String {

	val bl = Settings[context].blacklist

	val blMap = bl.groupBy { it.endsWith("%") }

	return buildString {

		append("${MediaStore.Audio.Media.IS_MUSIC}=1")
		if (bl.isEmpty()) return@buildString


		if (blMap[true] != null && blMap[true]?.isNotEmpty() == true) {
			blMap[true]?.forEachIndexed { _, s ->
				append(" AND ", "${MediaStore.Audio.Media.DATA} NOT LIKE \"$s\"")
			}
		}
		if (blMap[false] != null && blMap[false]?.isNotEmpty() == true)
			append(" AND ", "${MediaStore.Audio.Media.DATA} NOT IN (${blMap[false]?.joinToString(",") { "\"$it\"" }})")

	}

}

fun createPlaylist(context: Context, title: String): Int {

	context.contentResolver.query(
		MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
		arrayOf(MediaStore.Audio.Playlists.NAME),
		"${MediaStore.Audio.Playlists.NAME}=?",
		arrayOf(title),
		null
	).use {

		if (it != null && it.count >= 1) {
			Toast.makeText(context, "A playlist with that name exist", Toast.LENGTH_SHORT).show()
			return -1
		}

	}

	val cv = contentValuesOf(MediaStore.Audio.Playlists.NAME to title)

	return context.contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cv)?.apply {
		context.contentResolver.notifyChange(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null
		)
	}?.lastPathSegment?.toIntOrNull() ?: 0


}
fun addToPlaylist(context: Context, songs: Tracklist, playlistId: Int) {

	val tableUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId.toLong())
	var base = 0
	context.contentResolver.query(
		tableUri,
		arrayOf("max(${MediaStore.Audio.Playlists.Members.PLAY_ORDER})"),
		null, null, null
	).use {

		it?.moveToFirst()
		base = (it?.getInt(0) ?: 0) + 1

	}
	context.contentResolver.query(
		tableUri,
		arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID),
		null, null, null
	).use {
		if (it?.count == 0) return@use
		it.moveToFirst()
		val ids = songs.map { song -> song.id.toLong() }.toMutableList()

		val sqnc = sequence { do yield(it) while (it.moveToNext()) }
		for (crs in sqnc) {
			val long = crs.getLong(0)
			if (long in ids) {
				val i = ids.indexOf(long)
				ids.removeAt(i)
				songs.removeAt(i)
			}
		}

	}

	var cvs = arrayOf<ContentValues>()

	songs.forEachIndexed { i, song ->
		val cv = ContentValues()
		cv.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, i + base)
		cv.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, song.id)
		cvs += cv
	}

	context.contentResolver.bulkInsert(tableUri, cvs)
	context.contentResolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)

}
