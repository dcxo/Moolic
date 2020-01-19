package ml.dcxo.x.obwei.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.shape.*
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.coroutines.launch
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.DetailsTrackBottomDialog
import ml.dcxo.x.obwei.ui.dialogs.bottomDialogs.NotifyRemoveFun
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.*

/**
 * Created by David on 29/01/2019 for ObweiX
 */
abstract class BaseDetailsFragment<Item: Model> : BaseFragment() {

	lateinit var liveDataItem: LiveData<Item>

	@get:DrawableRes abstract val errorResource: Int

	var liveMiniPlayerVisibility: LiveData<Boolean>? = null
	var notifyRemove: NotifyRemoveFun = {
		view?.detailsRv?.adapter?.notifyItemRemoved(it)
	}

	private val observer = Observer<Item> {

		view?.let { view ->
			view.albumTitle?.text = it.getTitle()
			(view.detailsRv?.adapter as BaseAdapter<Song,*>).data = it.getData()

			launch {
				val imageUrl = it.getImageUrl(view.context)
				GlideApp.with(view.albumArt).asAmbitmap()
					.load(imageUrl)
					.centerCrop()
					.error(errorResource)
					.into( object: MukolorTarget(view.albumArt) {

						override fun onColorsReady(ambiColor: AmbiColor) {
							view.setBackgroundColor(ambiColor.color)
							view.toolbar.navigationIcon?.setTint(ambiColor.textColor)
							view.albumTitle.setTextColor(ambiColor.color)
							(view.bgHolder.background as MaterialShapeDrawable).setTint(ambiColor.bgColor)

							val nSecondaryTextColor = ColorUtils.setAlphaComponent(ambiColor.textColor, 255)
							if (Build.VERSION.SDK_INT >= 23) {

								when (nSecondaryTextColor) {
									Color.BLACK -> activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
									Color.WHITE -> activity?.window?.decorView?.systemUiVisibility = 0
								}

							}
						}
						override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {
							super.onResourceReady(resource, transition)
							launch { addToListener(it, resource.bitmap, imageUrl) }
						}

					})

			}
		}

	}

	override val layoutInflated: Int = R.layout.fragment_details

	abstract fun getAdapter(): BaseAdapter<Song, *>
	abstract fun Item.getTitle(): String
	abstract fun Item.getData(): ArrayList<Song>
	abstract suspend fun Item.getImageUrl(ctx: Context): String

	open fun addToListener(item: Item, resource: Bitmap?, model: String) {}

	override fun editOnCreateView(view: View) {
		super.editOnCreateView(view)

		view.setOnClickListener {}
		view.toolbar.setNavigationOnClickListener { (activity as? UniqueActivity)?.closeDetails() }
		(view.toolbar.layoutParams as ConstraintLayout.LayoutParams).updateMargins(top = statusBarSize + 8.dp)

		view.bgHolder.background = MaterialShapeDrawable(ShapePathModel().apply {
			val roundedCornerTreatment = RoundedCornerTreatment(8.dp.toFloat())
			topLeftCorner = roundedCornerTreatment
			topRightCorner = roundedCornerTreatment
		}).apply {
			setTint(Color.WHITE)
			shadowRadius = 8.dp
			shadowElevation = 8.dp
		}

		view.detailsRv.layoutManager = LinearLayoutManager(view.context)
		view.detailsRv.adapter = getAdapter().apply {
			click = { song, i ->
				uiInteractions?.onSongSelected(song, data, i)
			}
			longClick = { song, i ->
				fragmentManager?.let { it1 ->
					DetailsTrackBottomDialog
						.newInstance(song, this@BaseDetailsFragment, i)
						.show(it1, albumTrackBottomDialogFragmentTag)
				}
			}
		}

		liveMiniPlayerVisibility?.observe(this, Observer {
			this.view?.detailsRv?.updatePadding(bottom = if (it) 35.dp else 0)
		})
		liveDataItem.observe(this, observer)

	}
	override fun onDestroyView() {
		super.onDestroyView()
		if (Build.VERSION.SDK_INT >= 23) activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
	}

}