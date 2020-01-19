package ml.dcxo.x.obwei.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.*
import com.bumptech.glide.annotation.*
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.DispatcherAsyncTask
import ml.dcxo.x.obwei.AmbiColor
import kotlin.coroutines.CoroutineContext

/**
 * Created by David on 22/02/2019 for ObweiX
 */
@GlideModule class MukolorGlideModule: AppGlideModule() {

	override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
		val ambiColorTranscoder = AmbiColorTranscoder()
		registry.register(Bitmap::class.java, Ambitmap::class.java, ambiColorTranscoder)
	}
	override fun isManifestParsingEnabled(): Boolean = false

}
@GlideExtension object MukolorGlideExtension {

	@GlideType(Ambitmap::class) @JvmStatic fun asAmbitmap(requestBuilder: RequestBuilder<Ambitmap>) {}

}

abstract class MukolorTarget(view: ImageView): ImageViewTarget<Ambitmap>(view), CoroutineScope {

	override val coroutineContext: CoroutineContext = Dispatchers.Main

	abstract fun onColorsReady(ambiColor: AmbiColor)

	override fun setResource(resource: Ambitmap?) {
		if (resource != null) view.setImageBitmap(resource.bitmap)
	}

	override fun onLoadFailed(errorDrawable: Drawable?) {
		super.onLoadFailed(errorDrawable)
		onColorsReady(AmbiColor.NULL)
	}
	override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {
		super.onResourceReady(resource, transition)
		launch { onColorsReady( resource.ambiColor.await() ) }
	}

	companion object {

		operator fun invoke(view: ImageView, body: MukolorTarget.(AmbiColor)->Unit): MukolorTarget {
			return object : MukolorTarget(view) {
				override fun onColorsReady(ambiColor: AmbiColor) = body(ambiColor)
			}
		}

	}

}

data class Ambitmap(
	val bitmap: Bitmap,
	val ambiColor: Deferred<AmbiColor>
)
class AmbitmapResource(private val ambitmap: Ambitmap): Resource<Ambitmap> {
	override fun getResourceClass() = Ambitmap::class.java
	override fun get() = ambitmap
	override fun getSize() = ambitmap.bitmap.allocationByteCount
	override fun recycle() {
		ambitmap.bitmap.recycle()
		ambitmap.ambiColor.cancel()
	}
}
class AmbiColorTranscoder : ResourceTranscoder<Bitmap, Ambitmap>, CoroutineScope {
	override val coroutineContext: CoroutineContext = DispatcherAsyncTask
	override fun transcode(toTranscode: Resource<Bitmap>, options: Options): Resource<Ambitmap>? {

		val b = toTranscode.get()
		val dac = AmbiColor.Builder().setBitmap(b).buildAsync()
		val ambitmap = Ambitmap(b, dac)

		return AmbitmapResource(ambitmap)

	}
}