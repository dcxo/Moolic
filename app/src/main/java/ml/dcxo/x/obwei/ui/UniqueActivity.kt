package ml.dcxo.x.obwei.ui

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.*
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.*
import kotlinx.android.synthetic.main.activity_unique.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.AmbiColor
import ml.dcxo.x.obwei.service.*
import ml.dcxo.x.obwei.ui.fragments.PermissionsFragment
import ml.dcxo.x.obwei.ui.fragments.PlayerFragment
import ml.dcxo.x.obwei.ui.fragments.details.AlbumDetailsFragment
import ml.dcxo.x.obwei.ui.fragments.details.ArtistDetailsFragment
import ml.dcxo.x.obwei.ui.fragments.metadataEditors.AlbumMetadataEditorFragment
import ml.dcxo.x.obwei.ui.fragments.nav.*
import ml.dcxo.x.obwei.utils.*
import ml.dcxo.x.obwei.viewModel.*
import ml.dcxo.x.obwei.viewModel.providers.SongsProvider
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class UniqueActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
	BottomNavigationView.OnNavigationItemReselectedListener, CoroutineScope, ServicePetitions {

	override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

	var permissionsGranted = false
	var serviceState: LiveData<MusicService.ServiceState>? by Delegates.observable(MutableLiveData()) {_, _, newValue ->
		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) as? PlayerFragment
		newValue?.observe(this, serviceStateObserver)
		fragment?.liveStateService = newValue
	}
	var queue: LiveData<Tracklist>? by Delegates.observable(MutableLiveData()) {_, _, newValue ->
		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) as? PlayerFragment
		fragment?.liveQueue = newValue
	}
	var playerPosition: LiveData<Int>? by Delegates.observable(MutableLiveData()) {_, _, newValue ->
		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) as? PlayerFragment
		fragment?.livePlayerProgress = newValue
	}
	var audioSessionId = 0

	var uiInteractions: UIInteractions? = null
	var cServiceState: MusicService.ServiceState? = null

	val liveMiniPlayerVisibility = MutableLiveData<Boolean>()
	val obweiViewModel: ObweiViewModel by lazy { ViewModelProviders.of(this).get(ObweiViewModel::class.java) }

	private val serviceStateObserver = Observer<MusicService.ServiceState> {

		if (it.playbackState != cServiceState?.playbackState) setPlaybackState(it.playbackState)
		songTitle.text = it.song.title

		if (it.playbackState.state != PlaybackStateCompat.STATE_NONE) {
			GlideApp.with(this).asAmbitmap().load(it.song.getAlbumArtURI)
				.into(object : CustomViewTarget<View, Ambitmap>(root) {
					override fun onResourceReady(resource: Ambitmap, transition: Transition<in Ambitmap>?) {

						launch { setColors(resource.ambiColor.await()) }

					}

					override fun onResourceCleared(placeholder: Drawable?) {}
					override fun onLoadFailed(errorDrawable: Drawable?) {
						setColors(AmbiColor.NULL)
					}
				})
		} else {
			setColors(AmbiColor.NULL)
		}

		cServiceState = it

	}
	private val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			service as MusicService.MusicServiceBinder

			service.setServicePetitions(this@UniqueActivity as ServicePetitions)

			uiInteractions = service.uiInteractions

			serviceState = service.liveData.first
			queue = service.liveData.second
			playerPosition = service.liveData.third

			audioSessionId = service.getAudioSessionId()

			serviceState?.observe(this@UniqueActivity, serviceStateObserver)

		}
		override fun onServiceDisconnected(name: ComponentName?) {
			uiInteractions = null

			serviceState = null
			queue = null
			playerPosition = null

		}
	}

	fun showAlbum(song: Song) {

		val a = obweiViewModel.getAlbums()
		val b = Transformations.switchMap<ArrayList<Album>, Album>(a) {
			val album = it.find { album -> song.albumTitle == album.title } ?: throw IllegalStateException()
			return@switchMap MutableLiveData<Album>().apply { value = album }
		}
		showAlbum(b)

	}
	fun showAlbum(album: LiveData<Album>) {
		val f = AlbumDetailsFragment().apply { liveDataItem = album }
		f.liveMiniPlayerVisibility = liveMiniPlayerVisibility
		supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				R.anim.slide_up,
				R.anim.slide_down,
				R.anim.slide_up,
				R.anim.slide_down
			)
			.replace(R.id.detailsHost, f, detailsFragmentTag)
			.commit()

	}
	fun showArtist(song: Song) {

		val a = obweiViewModel.getArtist()
		val b = Transformations.switchMap<ArrayList<Artist>, Artist>(a) {
			val artist = it.firstOrNull { artist -> song.artistName == artist.name }
			return@switchMap MutableLiveData<Artist>().apply { value = artist }
		}
		showArtist(b)

	}
	fun showArtist(artist: LiveData<Artist>) {
		val f = ArtistDetailsFragment().apply { liveDataItem = artist }
		f.liveMiniPlayerVisibility = liveMiniPlayerVisibility
		supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				R.anim.slide_up,
				R.anim.slide_down,
				R.anim.slide_up,
				R.anim.slide_down
			)
			.replace(R.id.detailsHost, f, detailsFragmentTag)
			.commit()

	}
	fun closeDetails(): Boolean {

		val f = supportFragmentManager.findFragmentByTag(detailsFragmentTag) ?: return false
		if (!f.isVisible) return false

		supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				R.anim.slide_up,
				R.anim.slide_down,
				R.anim.slide_up,
				R.anim.slide_down
			)
			.remove(f)
			.commit()
		return true

	}
	fun hideKeyboard() {

		val imm = getSystemService<InputMethodManager>()
		imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

	}
	fun handlePermissions(permissionsGranted: Boolean) {
		if (permissionsGranted) {
			navMenu.setOnNavigationItemSelectedListener(this)
			navMenu.selectedItemId = R.id.homeMenuOption
			navMenu.setOnNavigationItemReselectedListener(this)
		}
	}

	private fun closeMetadataEditor(): Boolean {

		val f = supportFragmentManager.findFragmentByTag(metadataEditorFragmentTag) ?: return false
		if (!f.isVisible) return false

		supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				R.anim.slide_up,
				R.anim.slide_down,
				R.anim.slide_up,
				R.anim.slide_down
			)
			.hide(f)
			.commit()
		return true
	}
	private fun setColors(ambiColor: AmbiColor) {

		val path = ShapePathModel().apply {
			val cornerTreatment = RoundedCornerTreatment(4.dp.toFloat())
			topLeftCorner = cornerTreatment
			topRightCorner = cornerTreatment
		}
		val mDrawable = MaterialShapeDrawable(path).apply {
			setTint(if (ambiColor != AmbiColor.NULL) ambiColor.color else Color.WHITE)
			shadowElevation = 8.dp
		}
		miniPlayer.background = mDrawable

		songTitle.setTextColor(ambiColor.textColor)

		playPauseButton.clearColorFilter()
		playPauseButton.setColorFilter(ambiColor.textColor, PorterDuff.Mode.SRC_IN)

		navMenu.setBackgroundColor(if (ambiColor != AmbiColor.NULL) ambiColor.color else Color.WHITE)

		val cList = if (ambiColor == AmbiColor.NULL)
			ContextCompat.getColorStateList(this, R.color.selector_items_bottom_nav)
		else ColorStateList(
			arrayOf(
				intArrayOf(android.R.attr.state_checked),
				intArrayOf(-android.R.attr.state_checked)
			),
			intArrayOf(
				ColorUtils.setAlphaComponent(ambiColor.textColor, 255),
				ColorUtils.setAlphaComponent(ambiColor.textColor, 255/3)
			)
		)
		navMenu.itemIconTintList = cList
		navMenu.itemTextColor = cList


	}
	private fun setPlaybackState(state: PlaybackStateCompat) {

		when (state.state) {
			PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_PLAYING -> miniPlayer.visibility = View.VISIBLE
			PlaybackStateCompat.STATE_NONE -> miniPlayer.visibility = View.GONE
		}
		liveMiniPlayerVisibility.postValue(miniPlayer.isVisible)

		when (state.state) {
			PlaybackStateCompat.STATE_PAUSED ->
				playPauseButton.setImageDrawable(
					AnimatedVectorDrawableCompat.create(this, R.drawable.icon_pause_play) )
			PlaybackStateCompat.STATE_PLAYING ->
				playPauseButton.setImageDrawable(
					AnimatedVectorDrawableCompat.create(this, R.drawable.icon_play_pause) )
		}

		(playPauseButton.drawable as? AnimatedVectorDrawableCompat)?.start()

	}

	override fun onCreate(savedInstanceState: Bundle?) {

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_unique)

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
			&& ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.navHost, PermissionsFragment()).commit()
		} else { permissionsGranted = true }

		val serviceIntent = Intent(this, MusicService::class.java)
		startService(serviceIntent)
		bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE)

		miniPlayer.setOnClickListener { openPlayer() }
		playPauseButton.setOnClickListener { uiInteractions?.onPlayPauseButtonClicked() }

		if (permissionsGranted) {
			navMenu.setOnNavigationItemSelectedListener(this)
			navMenu.selectedItemId = savedInstanceState?.getInt(lastTab, R.id.homeMenuOption) ?: R.id.homeMenuOption
			navMenu.setOnNavigationItemReselectedListener(this)
		}

	}
	override fun onResume() {
		super.onResume()
		when (intent.action) {
			OPEN_PLAYER        -> openPlayer()
			Intent.ACTION_VIEW -> {
				val s = intent.data?.path
				s?.let {
					contentResolver.query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						SongsProvider.projection,
						"${MediaStore.Audio.Media.DATA}=?",
						arrayOf(it), null
					).use { crs ->

						if (!crs.moveToFirst()) return@use

						val song = SongsProvider.createSong(crs)
						val songIntent = Intent(this, MusicService::class.java).apply {
							action = MusicService.PLAY_SONG
							putExtra(MusicService.SONG, song)
						}
						startService(songIntent)

					}

					intent.setData(null)

				}
			}
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {

		outState.putInt(lastTab, navMenu.selectedItemId)

		super.onSaveInstanceState(outState)
	}
	override fun onBackPressed() {

		if (closeMetadataEditor())
		else if (closePlayer())
		else if (closeDetails())
		else super.onBackPressed()

	}
	override fun onStop() {
		super.onStop()

		launch(DispatcherAsyncTask) { Glide.get(this@UniqueActivity).clearDiskCache() }

		Glide.get(this).clearMemory()
		System.gc()

		unbindService(serviceConnection)

	}
	override fun onRestart() {
		super.onRestart()
		val serviceIntent = Intent(this, MusicService::class.java)
		bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE)
	}
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

		if (requestCode == 0xCD) {
			permissions.forEachIndexed {index, s ->
				val result = grantResults[index]
				permissionsGranted = result == PackageManager.PERMISSION_GRANTED
			}

		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		when (requestCode) {
			2019 -> {
				if (resultCode == Activity.RESULT_OK) {
					val uri = data?.data
					val fragment = supportFragmentManager.findFragmentByTag(metadataEditorFragmentTag) as? AlbumMetadataEditorFragment
					fragment?.imageFromActivityResult(uri)
				}
			}
		}

	}

	override fun onNavigationItemSelected(p0: MenuItem): Boolean {

		closeDetails()

		val navFragment = when (p0.itemId) {
			R.id.homeMenuOption -> HomeNavFragment()
			R.id.songsMenuOption -> SongsNavFragment()
			R.id.albumsMenuOption -> AlbumsNavFragment()
			R.id.artistsMenuOption -> ArtistsNavFragment()
			else -> return false
		}

		supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				android.R.anim.fade_in,
				android.R.anim.fade_out,
				android.R.anim.fade_in,
				android.R.anim.fade_out
			)
			.replace(R.id.navHost, navFragment, navFragmentsKey)
			.commit()

		return true

	}
	override fun onNavigationItemReselected(p0: MenuItem) {

		if (!closeDetails())
			return

	}

	override fun openPlayer() {

		bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)

		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) as? PlayerFragment
		val transaction = supportFragmentManager.beginTransaction().setCustomAnimations(
			android.R.anim.fade_in,
			android.R.anim.fade_out,
			android.R.anim.fade_in,
			android.R.anim.fade_out
		)
		if (fragment == null) {
			val newFragment = PlayerFragment().apply {
				livePlayerProgress = playerPosition
				liveStateService = serviceState
				liveQueue = queue
			}

			transaction.add(R.id.topContainer, newFragment, playerFragmentTag)
		} else {
			transaction.show(fragment)
		}
		transaction.commitAllowingStateLoss()

	}
	override fun closePlayer(destroy: Boolean): Boolean {

		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) ?: return false
		if (!fragment.isVisible) return false

		val transaction = supportFragmentManager.beginTransaction().setCustomAnimations(
			android.R.anim.fade_in,
			android.R.anim.fade_out,
			android.R.anim.fade_in,
			android.R.anim.fade_out
		)
		if (destroy)
			transaction.remove(fragment)
		else
			transaction.hide(fragment)
		transaction.commitAllowingStateLoss()

		return true

	}

	companion object {
		const val OPEN_PLAYER = "X_OPEN_PLAYER_X"
		private const val lastTab = "X_LAST_TAB_NAV_X"
	}

}
