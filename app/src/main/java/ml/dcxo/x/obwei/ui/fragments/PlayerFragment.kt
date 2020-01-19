package ml.dcxo.x.obwei.ui.fragments

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.media.audiofx.AudioEffect
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.*
import android.widget.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.*
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_albumart.view.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import kotlinx.coroutines.launch
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.QueueAdapter
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.service.MusicService
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.SongMetadataEditorFragment
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist
import java.util.*
import kotlin.properties.Delegates

/**
 * Created by David on 02/11/2018 for XOXO
 */
class PlayerFragment: BaseFragment() {

	var liveStateService: LiveData<MusicService.ServiceState>? by Delegates.observable(MutableLiveData()) { _, _, newValue ->
		newValue?.observe(this, stateServiceObserver)
	}
	var liveQueue: LiveData<Tracklist>? by Delegates.observable(MutableLiveData()) {_, _, newValue ->
		newValue?.observe(this, queueObserver)
	}
	var livePlayerProgress: LiveData<Int>? by Delegates.observable(MutableLiveData()) {_, _, newValue ->
		newValue?.observe(this, playerProgressObserver)
	}
	var liveColor: MutableLiveData<AmbiColor> = MutableLiveData()

	private var width = 1
	private var cServiceState: MusicService.ServiceState? = null
	private var moveRecycler = true
	private var runIndexSelected = true

	private val stateServiceObserver = Observer<MusicService.ServiceState> {

		if (it.song.id != cServiceState?.song?.id) setSongInfo(it.song)
		if (it.position != cServiceState?.position) updatePosition(it.position)

		if (it.shuffleMode != cServiceState?.shuffleMode) setShuffleMode(it.shuffleMode)
		if (it.repeatMode != cServiceState?.repeatMode) setRepeatMode(it.repeatMode)
		//setColors(it.ambiColor)
		if (it.playbackState.state != cServiceState?.playbackState?.state) setPlaybackState(it.playbackState)

		cServiceState = it

	}
	private val queueObserver = Observer<Tracklist> { setQueue(it) }
	private val colorsObserver = Observer<AmbiColor> { setColors(it) }
	private val playerProgressObserver = Observer<Int> {

		view?.progressSeekBar?.progress = it
		view?.currentPlayerProgress?.text = millisToString(it.toLong())

	}
	private val onPageChangeListener = object: ViewPager.OnPageChangeListener{
		override fun onPageSelected(position: Int) {

			view?.viewPager?.isFakeDragging
			if (position != cServiceState?.position && runIndexSelected)
				uiInteractions?.onIndexSelected(position % (liveQueue?.value ?: Tracklist()).size)

		}

		override fun onPageScrollStateChanged(state: Int) {}
		override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
	}
	private val itemTouchHelper = ItemTouchHelper(
		object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
			var isDragging by Delegates.observable(ItemTouchHelper.ACTION_STATE_IDLE) {_, oldValue, newValue ->
				if (oldValue == ItemTouchHelper.ACTION_STATE_DRAG && newValue == ItemTouchHelper.ACTION_STATE_IDLE)
					uiInteractions?.onQueueChange(
						(view?.queueRv?.adapter as? QueueAdapter)?.data ?: arrayListOf() )
			}

			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {

				val from = viewHolder.adapterPosition
				val to = target.adapterPosition
				Collections.swap((view?.queueRv?.adapter as? QueueAdapter)?.data, from, to)
				view?.queueRv?.adapter?.notifyItemMoved(from, to)
				return isDragging == ItemTouchHelper.ACTION_STATE_IDLE

			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				uiInteractions?.onRemoveFromQueue(viewHolder.adapterPosition)
			}

			override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
				super.onSelectedChanged(viewHolder, actionState)

				isDragging = actionState

			}
		})

	override val layoutInflated: Int = R.layout.fragment_player

	private fun RecyclerView.scrollToCenter(position: Int) {

		if (layoutManager is LinearLayoutManager)
			(layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 4.dp)

	}
	private fun setColors(ambiColor: AmbiColor) {
		view?.apply {

			val colorGradient = ambiColor.color
			if (background == null) {
				setBackgroundColor(colorGradient)
			} else {

				val i = (background as ColorDrawable).color
				ObjectAnimator.ofArgb(i, ambiColor.color).apply {
					interpolator = FastOutSlowInInterpolator()
					duration = 440
					addUpdateListener {
						setBackgroundColor(it.animatedValue as Int)
					}
				}.start()

			}

			val nSecondaryTextColor = ColorUtils.setAlphaComponent(ambiColor.textColor, 255)
			if (SDK_INT >= 23) {

				activity?.window?.decorView?.systemUiVisibility =  when (nSecondaryTextColor) {
					Color.BLACK -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
					Color.WHITE -> 0
					else        -> 0
				}

			}

			playerSongTitle.setTextColor(nSecondaryTextColor)
			artistName.setTextColor(nSecondaryTextColor)

			toolbar.menu.forEach {
				it.icon.setTint(nSecondaryTextColor)
			}
			toolbar.overflowIcon?.setTint(nSecondaryTextColor)
			toolbar.navigationIcon?.setTint(nSecondaryTextColor)

			currentPlayerProgress.setTextColor(ambiColor.bgColor)
			songDuration.setTextColor(ambiColor.bgColor)

			slideMsg.setTextColor(ambiColor.color)
			queueTitle.setTextColor(ambiColor.color)

			floatingActionButton.backgroundTintList = ColorStateList.valueOf(ambiColor.bgColor)
			playerControls.setCardBackgroundColor(ambiColor.bgColor)
			queueCard.setCardBackgroundColor(ambiColor.bgColor)

			shuffleButton.clearColorFilter()
			shuffleButton.setColorFilter(ambiColor.color, PorterDuff.Mode.SRC_IN)
			repeatButton.clearColorFilter()
			repeatButton.setColorFilter(ambiColor.color, PorterDuff.Mode.SRC_IN)
			floatingActionButton.clearColorFilter()
			floatingActionButton.setColorFilter(ambiColor.color, PorterDuff.Mode.SRC_IN)
			imageView.clearColorFilter()
			imageView.setColorFilter(ambiColor.color, PorterDuff.Mode.SRC_IN)
			imageView2.clearColorFilter()
			imageView2.setColorFilter(ambiColor.color, PorterDuff.Mode.SRC_IN)

			val l = (progressSeekBar.progressDrawable as LayerDrawable).apply {
				setDrawableByLayerId(
					android.R.id.background,
					makeBackgroundDrawableForSeekBar(ambiColor.color)
				)
				setDrawableByLayerId(
					android.R.id.secondaryProgress,
					ColorDrawable(Color.TRANSPARENT)
				)
				setDrawableByLayerId(
					android.R.id.progress,
					ColorDrawable(Color.TRANSPARENT)
				)
			}
			progressSeekBar.progressDrawable = l
			progressSeekBar.thumb.setColorFilter(ambiColor.bgColor, PorterDuff.Mode.SRC_IN)

		}
	}
	private fun setSongInfo(song: Song) {

		view?.playerSongTitle?.text = song.title
		view?.artistName?.text = song.artistName
		view?.progressSeekBar?.max = song.duration.toInt()
		view?.songDuration?.text = millisToString(song.duration)

	}
	private fun setShuffleMode(shuffleMode: Boolean) {

		if (shuffleMode) shuffleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_shuffle, null))
		else shuffleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_shuffle_off, null))

	}
	private fun setRepeatMode(repeatMode: Int) {

		when (repeatMode) {

			PlaybackStateCompat.REPEAT_MODE_NONE -> repeatButton.setImageDrawable(resources.getDrawable(R.drawable.icon_repeat_off, null))
			PlaybackStateCompat.REPEAT_MODE_ONE -> repeatButton.setImageDrawable(resources.getDrawable(R.drawable.icon_repeat_one, null))
			PlaybackStateCompat.REPEAT_MODE_ALL -> repeatButton.setImageDrawable(resources.getDrawable(R.drawable.icon_repeat, null))

		}

	}
	private fun setPlaybackState(playbackState: PlaybackStateCompat) {

		when (playbackState.state) {
			PlaybackStateCompat.STATE_PAUSED ->
				view?.floatingActionButton?.setImageDrawable(
					AnimatedVectorDrawableCompat.create(view!!.context, R.drawable.icon_pause_play) )
			PlaybackStateCompat.STATE_PLAYING ->
				view?.floatingActionButton?.setImageDrawable(
					AnimatedVectorDrawableCompat.create(view!!.context, R.drawable.icon_play_pause) )
		}

		(view?.floatingActionButton?.drawable as? AnimatedVectorDrawableCompat)?.start()

	}
	private fun updatePosition(position: Int) {

		runIndexSelected = false
		view?.viewPager?.setCurrentItem(position, true)
		runIndexSelected = true
		(view?.queueRv?.adapter as? QueueAdapter)?.currentPosition = position
		if (moveRecycler) view?.queueRv?.scrollToCenter(position)

	}
	private fun setQueue(it: Tracklist) {

		view?.viewPager?.adapter = AlbumArtsAdapter().apply { data = it }
		view?.viewPager?.currentItem = cServiceState?.position ?: 0

		(view?.queueRv?.adapter as? QueueAdapter)?.data = it

	}
	private fun startDragFunc(holder: QueueAdapter.QueueVH) {
		itemTouchHelper.startDrag(holder)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		liveColor.observe(this, colorsObserver)
	}
	override fun editOnCreateView(view: View) {

		view.setOnClickListener {}
		view.background = null

		view.queueRv.apply {
			this.adapter = QueueAdapter(this@PlayerFragment::startDragFunc).apply {
				click = { _, i -> if (i != cServiceState?.position) uiInteractions?.onIndexSelected(i) }
			}
			this.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)

			if (this.itemDecorationCount > 0) this.removeItemDecorationAt(0)
			this.addItemDecoration(MarginDecor(8.dp, 4.dp), 0)

			itemTouchHelper.attachToRecyclerView(this)
		}

		view.layoutRoot.setTransitionListener(object : MotionLayout.TransitionListener {

			override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
			override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}
			override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

				imageView.rotation = p3*180
				imageView2.rotation = p3*180

			}
			override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
				when (p1) {
					R.id.queueStart -> {
						moveRecycler = true
						this@PlayerFragment.view?.queueRv?.scrollToCenter(cServiceState?.position ?: 0)
					}
					R.id.queueEnd   -> moveRecycler = false
				}

			}

		})

		view.toolbar.updatePadding(top = statusBarSize)
		view.toolbar.inflateMenu(R.menu.player_menu)
		view.toolbar.setOnMenuItemClickListener {

			when (it.itemId) {
				R.id.eqOption -> {
					val i = (activity as? UniqueActivity)?.audioSessionId ?: 0
					val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
						putExtra(AudioEffect.EXTRA_AUDIO_SESSION, i)
						putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
					}
					(activity)?.startActivityForResult(intent, 0)
				}
				R.id.removeOption -> uiInteractions?.onRemoveQueue()
				R.id.editMetadataOption -> cServiceState?.song?.let { song ->
					fragmentManager?.beginTransaction()
						?.add(
							R.id.metadataEditorContainer,
							SongMetadataEditorFragment().apply { obj = song },
							metadataEditorFragmentTag
						)?.commit()
				}
			}

			true
		}
		view.toolbar.setNavigationIcon(R.drawable.icon_back)
		view.toolbar.setNavigationOnClickListener { mActivity?.closePlayer() }

		view.floatingActionButton.setOnClickListener { uiInteractions?.onPlayPauseButtonClicked() }
		view.shuffleButton.setOnClickListener { uiInteractions?.onShuffleButtonClicked() }
		view.repeatButton.setOnClickListener { uiInteractions?.onRepeatButtonClicked() }

		view.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
			override fun onStartTrackingTouch(seekBar: SeekBar?) {}
			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				uiInteractions?.onSeekBarProgressChange(seekBar?.progress ?: 0)
			}
		})

		view.viewPager.addOnPageChangeListener(onPageChangeListener)
		view.viewPager.pageMargin = 16.dp
		view.viewPager.setPageTransformer(false) {page, position ->
			page.cardAlbumArt.cardElevation = ((-8).dp * Math.abs(position) + 8.dp)
		}
		view.viewPager.currentItem = liveStateService?.value?.position ?: 0

		view.progressSeekBar.thumb = makeThumbDrawableForSeekBar(Color.WHITE)

	}
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		val point = Point()
		view?.display?.getSize(point)
		width = point.x
		view?.viewPager?.updatePadding(left = 46.dp, right = 46.dp)

		liveStateService = (activity as? UniqueActivity)?.serviceState
		livePlayerProgress = (activity as? UniqueActivity)?.playerPosition
		liveQueue = (activity as? UniqueActivity)?.queue

		view?.viewPager?.adapter?.notifyDataSetChanged()

	}
	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)

		if (hidden) {
			view?.layoutRoot?.progress = 0f
			if (SDK_INT >= 23) activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		} else {
			val nSecondaryTextColor = ColorUtils.setAlphaComponent(
				liveColor.value?.textColor ?: 0, 255
			)
			if (SDK_INT >= 23) {
				activity?.window?.decorView?.systemUiVisibility = when (nSecondaryTextColor) {
					Color.BLACK -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
					Color.WHITE -> 0
					else -> 0
				}
			}
		}

	}
	override fun onDestroyView() {
		super.onDestroyView()

		view?.let {
			it.setOnClickListener(null)
			it.layoutRoot.setTransitionListener(null)
			it.floatingActionButton.setOnClickListener(null)
			it.shuffleButton.setOnClickListener(null)
			it.repeatButton.setOnClickListener(null)
			it.progressSeekBar.setOnSeekBarChangeListener(null)
			it.viewPager.removeOnPageChangeListener(onPageChangeListener)
		}
		if (SDK_INT >= 23) activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

	}

	inner class AlbumArtsAdapter : FragmentStatePagerAdapter(childFragmentManager) {

		var data: Tracklist by Delegates.observable(Tracklist()) { _,_,_ -> notifyDataSetChanged() }

		override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
			super.setPrimaryItem(container, position, `object`)

			if (`object` is AlbumArtFragment)
				launch { liveColor.postValue(`object`.ambiColor?.await() ?: AmbiColor.NULL) }

		}
		override fun getItem(position: Int): AlbumArtFragment {
			return AlbumArtFragment.newInstance(data[position])
		}
		override fun getPageWidth(position: Int) = 1f - 6.dp.toFloat()/ width.toFloat()
		override fun getCount() = data.size

	}

}