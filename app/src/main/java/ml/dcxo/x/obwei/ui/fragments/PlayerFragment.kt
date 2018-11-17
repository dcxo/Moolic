package ml.dcxo.x.obwei.ui.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.*
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.adapters.SongsAdapter
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.service.MusicService
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.Song
import ml.dcxo.x.obwei.viewModel.Tracklist
import kotlin.properties.Delegates

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class PlayerFragment: BaseFragment() {

	lateinit var liveStateService: LiveData<MusicService.ServiceState>
	lateinit var liveQueue: LiveData<Tracklist>
	lateinit var livePlayerProgress: LiveData<Int>

	private var cServiceState: MusicService.ServiceState? = null

	private val stateServiceObserver = Observer<MusicService.ServiceState> {

		if (it.song.id != cServiceState?.song?.id) {
			setSongInfo(it.song)
		}
		if (it.position != cServiceState?.position) {
			view?.viewPager?.currentItem = it.position
		}
		if (it.shuffleMode != cServiceState?.shuffleMode) {
			setShuffleMode(it.shuffleMode)
		}
		if (it.repeatMode != cServiceState?.repeatMode) {
			setRepeatMode(it.repeatMode)
		}
		setColors(it.ambiColor)
		if (it.playbackState.state != cServiceState?.playbackState?.state) {
			setPlaybackState(it.playbackState)
		}

		cServiceState = it

	}
	private val queueObserver = Observer<Tracklist> {

		view?.viewPager?.adapter = AlbumArtsAdapter().apply { data = it }
		view?.viewPager?.currentItem = cServiceState?.position ?: 0
		view?.queueRv?.adapter = SongsAdapter().apply { updateData(it) }

	}
	private val playerProgressObserver = Observer<Int> {

		view?.progressSeekBar?.progress = it
		view?.currentPlayerProgress?.text = millisToString(it.toLong())

	}
	private val onPageChangeListener =  object : ViewPager.SimpleOnPageChangeListener(){
		override fun onPageSelected(position: Int) {

			if (position != cServiceState?.position) uiInteractions?.onIndexSelected(position)

		}
	}

	override val layoutInflated: Int = R.layout.fragment_player

	private fun setColors(ambiColor: AmbiColor) { view?.apply {

		val colorGradient = generateAmbiColorGradient(ambiColor)
		if (background == null) {
			background = colorGradient
		} else {

			val td = TransitionDrawable(arrayOf(background, colorGradient))
			background = td
			td.startTransition(440)

		}

		songTitle.setTextColor(ambiColor.secondaryTextColor)
		artistName.setTextColor(ambiColor.secondaryTextColor)

		currentPlayerProgress.setTextColor(ambiColor.primaryBgColor)
		songDuration.setTextColor(ambiColor.primaryBgColor)

		slideMsg.setTextColor(ambiColor.primaryColor)
		queueTitle.setTextColor(ambiColor.primaryColor)

		floatingActionButton.backgroundTintList = ColorStateList.valueOf(ambiColor.primaryBgColor)
		playerControls.setCardBackgroundColor(ambiColor.primaryBgColor)
		queueCard.setCardBackgroundColor(ambiColor.primaryBgColor)

		shuffleButton.clearColorFilter()
		shuffleButton.setColorFilter(ambiColor.primaryColor, PorterDuff.Mode.SRC_IN)
		backButton.clearColorFilter()
		backButton.setColorFilter(ambiColor.secondaryTextColor, PorterDuff.Mode.SRC_IN)
		repeatButton.clearColorFilter()
		repeatButton.setColorFilter(ambiColor.primaryColor, PorterDuff.Mode.SRC_IN)
		floatingActionButton.clearColorFilter()
		floatingActionButton.setColorFilter(ambiColor.primaryColor, PorterDuff.Mode.SRC_IN)
		imageView.clearColorFilter()
		imageView.setColorFilter(ambiColor.primaryColor, PorterDuff.Mode.SRC_IN)
		imageView2.clearColorFilter()
		imageView2.setColorFilter(ambiColor.primaryColor, PorterDuff.Mode.SRC_IN)

		val l = (progressSeekBar.progressDrawable as LayerDrawable).apply {
			setDrawableByLayerId(
				android.R.id.background,
				makeBackgroundDrawableForSeekBar(ambiColor.primaryColor)
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
		progressSeekBar.thumb.setColorFilter(ambiColor.primaryBgColor, PorterDuff.Mode.SRC_IN)

	}
	}
	private fun setSongInfo(song: Song) {

		view?.songTitle?.text = song.title
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

	override fun editOnCreateView(view: View) {

		view.setOnClickListener {}
		view.queueRv.layoutManager = LinearLayoutManager(view.context)
		view.background = null

		view.floatingActionButton.setOnClickListener { uiInteractions?.onPlayPauseButtonClicked() }
		view.backButton.setOnClickListener { fragmentManager?.beginTransaction()?.hide(this)?.commit() }
		view.shuffleButton.setOnClickListener { uiInteractions?.onShuffleButtonClicked() }
		view.repeatButton.setOnClickListener { uiInteractions?.onRepeatButtonClicked() }
		view.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

				if (fromUser) uiInteractions?.onSeekBarProgressChange(progress)

			}

			override fun onStartTrackingTouch(seekBar: SeekBar?) {}
			override fun onStopTrackingTouch(seekBar: SeekBar?) {}
		})
		view.viewPager.addOnPageChangeListener(onPageChangeListener)

		view.progressSeekBar.thumb = makeThumbDrawableForSeekBar(Color.WHITE)

		liveStateService.observe(this, stateServiceObserver)
		liveQueue.observe(this, queueObserver)
		livePlayerProgress.observe(this, playerProgressObserver)

	}
	override fun onDestroyView() {
		super.onDestroyView()

		view?.let {
			it.floatingActionButton.setOnClickListener(null)
			it.shuffleButton.setOnClickListener(null)
			it.repeatButton.setOnClickListener(null)
			it.progressSeekBar.setOnSeekBarChangeListener(null)
			it.viewPager.removeOnPageChangeListener(onPageChangeListener)
		}

	}

	companion object {

		fun newInstance(
			liveStateService: LiveData<MusicService.ServiceState>,
			liveQueue: LiveData<Tracklist>,
			livePlayerProgress: LiveData<Int>
		): PlayerFragment = PlayerFragment().apply {

			this.liveStateService = liveStateService
			this.liveQueue = liveQueue
			this.livePlayerProgress = livePlayerProgress

		}

	}

	inner class AlbumArtsAdapter: FragmentStatePagerAdapter(fragmentManager) {

		var data: Tracklist by Delegates.observable(arrayListOf()) {_, _, _ -> notifyDataSetChanged() }

		override fun getItem(position: Int): Fragment = AlbumArtFragment.newInstance( data[position % data.size] )

		override fun getCount(): Int = data.size // supports infinite viewpager

	}

}