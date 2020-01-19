package ml.dcxo.x.obwei.ui.fragments.metadataEditors

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.forEach
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.*
import kotlinx.android.synthetic.main.fragment_album_metadata_editor.view.*
import kotlinx.android.synthetic.main.fragment_base_metadata_editor.view.*
import kotlinx.coroutines.launch
import ml.dcxo.x.obwei.base.BaseMetadataEditorFragment
import ml.dcxo.x.obwei.viewModel.Album
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.utils.*
import org.jaudiotagger.tag.FieldKey

/**
 * Created by David on 05/02/2019 for ObweiX
 */
class AlbumMetadataEditorFragment: BaseMetadataEditorFragment<Album>() {

	override val layoutRes: Int = R.layout.fragment_album_metadata_editor

	fun imageFromActivityResult(uri: Uri?) {
		view?.let {

			GlideApp.with(it).asAmbitmap().load(uri)
				.into(object : MukolorTarget(it.albumArt) {
					override fun onLoadFailed(errorDrawable: Drawable?) {
						newBitmap = null
						super.onLoadFailed(errorDrawable)
					}
					override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {
						newBitmap = resource.bitmap
						super.onResourceReady(resource, transition)
					}
					override fun onColorsReady(ambiColor: AmbiColor) {
						setColors(ambiColor)
					}
				})

			imageChanged = true
			dataChanged()
		}
	}

	private fun loadFromLastFM(){

		launch {

			val title = view?.albumTitle?.editText?.text ?: ""
			val artistName = view?.artistName?.editText?.text ?: ""

			val json = LastFmService.service.getAlbumInfoJSON("$title", "$artistName").await()
			val jsonArray = json.getAsJsonObject("album")?.getAsJsonArray("image")
			val bitmapUrl = jsonArray?.find {
				it.asJsonObject.get("size").asString.toLowerCase() == "extralarge"
			}?.asJsonObject?.get("#text")?.asString ?: jsonArray?.find {
				it.asJsonObject.get("size").asString.toLowerCase() == "large"
			}?.asJsonObject?.get("#text")?.asString ?: ""

			view?.let {
				GlideApp.with(it).asAmbitmap().load(bitmapUrl)
					.into(object : MukolorTarget(it.albumArt) {
						override fun onLoadFailed(errorDrawable: Drawable?) {
							newBitmap = null
							super.onLoadFailed(errorDrawable)
						}
						override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {
							newBitmap = resource.bitmap
							super.onResourceReady(resource, transition)
						}
						override fun onColorsReady(ambiColor: AmbiColor) {
							setColors(ambiColor)
						}
					})
				imageChanged = true
				dataChanged()
			}

		}

	}
	private fun sendIntent(){

		val intent = Intent().apply {
			type = "image/*"
			action = Intent.ACTION_GET_CONTENT
		}
		activity?.startActivityForResult(Intent.createChooser(intent, "Get image"), 2019)

	}
	private fun removeAlbumArt(){

		view?.let {
			val color = ContextCompat.getColor(it.context, R.color.colorPrimary)

			it.albumArt.setImageResource(R.drawable.drawable_error_album_art_album)
			launch { setColors(AmbiColor.Builder().setColor(color).buildAsync().await()) }

			newBitmap = null
			imageChanged = true
			dataChanged()
		}

	}
	private fun setColors(color: AmbiColor) {
		view?.apply {
			bgHelper.setBackgroundColor(color.color)
			appBar.setBackgroundColor(color.color)
			editButton.apply {
				drawable?.setTint(color.color)
				background =
					MaterialShapeDrawable(ShapePathModel().apply {
						topLeftCorner = RoundedCornerTreatment(4.dp.toFloat())
					}).apply {
						tintList = ColorStateList.valueOf(color.bgColor)
						shadowRadius = 4.dp
					}
			}
			toolbar.apply {
				navigationIcon?.setTint(color.textColor)
				menu?.forEach { it.icon.setTint(color.textColor) }
			}
			val nSecondaryTextColor = ColorUtils.setAlphaComponent(color.textColor, 255)
			if (Build.VERSION.SDK_INT >= 23) {

				activity?.window?.decorView?.systemUiVisibility =  when (nSecondaryTextColor) {
					Color.BLACK -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
					Color.WHITE -> 0
					else        -> 0
				}

			}
		}
	}

	override fun Album.setItemData(view: View) {
		view.albumTitle.editText?.setText(title, TextView.BufferType.EDITABLE)
		view.artistName.editText?.setText(artistName, TextView.BufferType.EDITABLE)

		view.cardViewHelper.setOnClickListener {

			AlertDialog.Builder(it.context)
				.setNegativeButton(R.string.cancel) {_, _ ->  }
				.setItems(R.array.album_art_sources) {_, which ->
					when (which) {
						0 -> loadFromLastFM()
						1 -> sendIntent()
						2 -> removeAlbumArt()
						else -> Exception("WTF!")
					}
				}
				.show()

		}

		GlideApp.with(view).asAmbitmap().load(getAlbumArtURI)
			.into(MukolorTarget(view.albumArt) { setColors(it) })

	}
	override fun Album.getData(): List<String> = trackList.map { it.filePath }
	override fun getFieldsData(): Map<FieldKey, String> = mapOf(
		FieldKey.ALBUM        to (view?.albumTitle?.editText?.text?.toString() ?: obj.title),
		FieldKey.ALBUM_ARTIST to (view?.artistName?.editText?.text?.toString() ?: obj.artistName),
		FieldKey.ARTIST       to (view?.artistName?.editText?.text?.toString() ?: obj.artistName)
	)
	override fun Album.getAlbumId(): Int = id

}