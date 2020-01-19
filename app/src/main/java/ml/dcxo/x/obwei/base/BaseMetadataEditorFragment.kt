package ml.dcxo.x.obwei.base

import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem.SHOW_AS_ACTION_ALWAYS
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.contentValuesOf
import androidx.core.view.*
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_base_metadata_editor.view.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.DispatcherAsyncTask
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.viewModel.Model
import ml.dcxo.x.obwei.ui.dialogs.waitDialog
import ml.dcxo.x.obwei.utils.statusBarSize
import org.jaudiotagger.audio.*
import org.jaudiotagger.tag.FieldDataInvalidException
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by David on 31/01/2019 for ObweiX
 */
abstract class BaseMetadataEditorFragment<Item: Model>: BaseFragment(), TextWatcher {

	lateinit var obj: Item

	override val layoutInflated: Int = R.layout.fragment_base_metadata_editor
	@get:LayoutRes abstract val layoutRes: Int

	protected var imageChanged = false
	protected var newBitmap: Bitmap? = null
	private var inflatedView: View? = null

	private val fabClickListener = View.OnClickListener { saveNewInfo() }

	private fun getAudioFiles() = obj.getData().map { AudioFileIO.read( File(it) ) }

	private fun updateAlbumArt(ctx: Context, path: String?) {
		val contentResolver = ctx.contentResolver

		val artworkUri = Uri.parse("content://media/external/audio/albumart")
		contentResolver.delete(ContentUris.withAppendedId(artworkUri, obj.getAlbumId().toLong()), null, null)

		if (path != null) {
			val values = contentValuesOf(
				"album_id" to obj.getAlbumId().toLong(),
				"_data" to path
			)

			contentResolver.insert(artworkUri, values)
		}

	}
	private fun saveNewInfo() {

		context?.let { ctx ->
			val fields = getFieldsData()
			val filePaths = obj.getData()

			val albumArtsDir = File(Environment.getExternalStorageDirectory(), "/albumThumbs/")
			if (!albumArtsDir.exists()) {
				albumArtsDir.mkdirs()
				File(albumArtsDir, ".nomedia").createNewFile()
			}

			var artwork: Artwork? = null
			var albumArtFile: File? = null
			newBitmap?.let {
				albumArtFile = File(albumArtsDir, "${obj.getAlbumId()}").canonicalFile
				albumArtFile?.outputStream()?.use { ous ->
					it.compress(Bitmap.CompressFormat.PNG, 100, ous)
				}
				artwork = ArtworkFactory.createArtworkFromFile(albumArtFile)
			}

			val dialog = waitDialog(ctx)
			dialog.show()

			val j = launch(DispatcherAsyncTask) {
				for (path in filePaths) {
					val file = File(path).canonicalFile
					val audioFile = AudioFileIO.read(file)
					val tag = audioFile.tagOrCreateAndSetDefault
					for (field in fields) {
						if (field.value.isNotBlank()) tag.setField(field.key, field.value)
					}

					if (newBitmap != null && imageChanged) {
						tag.deleteArtworkField()
						try {
							tag.setField(artwork)
						} catch (e: UnsupportedOperationException) {
							Log.w("File Info", "\nWrite: ${file.canWrite()}\nRead: ${file.canRead()}")
						}
						updateAlbumArt(ctx, albumArtFile?.path)
					} else if (newBitmap == null && imageChanged) {
						tag.deleteArtworkField()
						updateAlbumArt(ctx, null)
					}

					withContext(Dispatchers.IO) { audioFile.commit() }
				}
			}
			j.invokeOnCompletion {

				val scan = MukolorScan(filePaths.toTypedArray(), activity, dialog)
				MediaScannerConnection.scanFile(activity, filePaths.toTypedArray(), null, scan)

			}
		}

	}
	protected fun dataChanged() {
		view?.toolbar?.menu?.get(0)?.isVisible = true
	}

	protected abstract fun Item.getAlbumId(): Int
	protected abstract fun Item.setItemData(view: View)
	protected abstract fun Item.getData(): List<String>
	protected abstract fun getFieldsData(): Map<FieldKey, String>

	override fun editOnCreateView(view: View) {
		super.editOnCreateView(view)

		view.setOnClickListener {}

		view.include.layoutResource = layoutRes
		inflatedView = view.include.inflate().also {
			(it as ConstraintLayout).children.forEach { child ->
				if (child is TextInputLayout) child.editText?.addTextChangedListener(this)
			}
		}
		view.toolbar.setNavigationOnClickListener { fragmentManager?.beginTransaction()?.remove(this)?.commit() }
		view.toolbar.menu.add("Save").apply {
			setIcon(R.drawable.icon_save)
			setShowAsAction(SHOW_AS_ACTION_ALWAYS)
			setOnMenuItemClickListener { saveNewInfo(); false }
			isVisible = false
		}
		view.setOnApplyWindowInsetsListener { _, insets ->

			(this.view ?: view).toolbar?.updatePadding(top = insets.systemWindowInsetTop)

			return@setOnApplyWindowInsetsListener insets
		}
		view.toolbar.updatePadding(top = statusBarSize)


		obj.setItemData(view)

	}
	override fun onDestroyView() {

		(inflatedView as? ConstraintLayout)?.children?.forEach { child ->
			if (child is TextInputLayout) child.editText?.removeTextChangedListener(this)
		}
		if (Build.VERSION.SDK_INT >= 23) activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

		super.onDestroyView()
	}

	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
	override fun afterTextChanged(s: Editable?) = dataChanged()

	inner class MukolorScan(
		var haveToScan: Array<String>,
		activity: Activity?,
		dialog: Dialog?
	): MediaScannerConnection.OnScanCompletedListener {

		private var toast: Toast? = null
		private var scanned = 0
		private var failed = 0

		private val weakActivity = WeakReference<Activity>(activity)
		private val weakDialog = WeakReference<Dialog>(dialog)

		init {
			activity?.runOnUiThread { toast = Toast.makeText(activity, "", Toast.LENGTH_LONG) }
		}

		override fun onScanCompleted(path: String?, uri: Uri?) {
			weakActivity.get()?.runOnUiThread {
				if (uri == null) failed++ else scanned++
				toast?.setText("$scanned/${haveToScan.size} Scanned")
				toast?.show()
				if (failed+scanned == haveToScan.size) {
					weakDialog.get()?.dismiss()
					fragmentManager?.beginTransaction()?.remove(this@BaseMetadataEditorFragment)?.commit()
				}
			}
		}
	}

}
