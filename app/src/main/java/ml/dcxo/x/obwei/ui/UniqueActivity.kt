package ml.dcxo.x.obwei.ui

import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_unique.*
import kotlinx.coroutines.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.service.*
import ml.dcxo.x.obwei.ui.fragments.PlayerFragment
import ml.dcxo.x.obwei.ui.fragments.nav.*
import ml.dcxo.x.obwei.utils.navFragmentsKey
import ml.dcxo.x.obwei.utils.playerFragmentTag
import ml.dcxo.x.obwei.viewModel.ObweiViewModel
import ml.dcxo.x.obwei.viewModel.Tracklist
import kotlin.coroutines.CoroutineContext

class UniqueActivity : AppCompatActivity(),
	BottomNavigationView.OnNavigationItemSelectedListener, CoroutineScope, ServicePetitions {

	override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

	lateinit var serviceState: LiveData<MusicService.ServiceState>
	lateinit var queue: LiveData<Tracklist>
	lateinit var playerPosition: LiveData<Int>
	var uiInteractions: UIInteractions? = null

	val obweiViewModel: ObweiViewModel by lazy { ViewModelProviders.of(this).get(ObweiViewModel::class.java) }
	private val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			service as MusicService.MusicServiceBinder

			service.setServicePetitions(this@UniqueActivity as ServicePetitions)
			uiInteractions = service.uiInteractions
			serviceState = service.liveData.first
			queue = service.liveData.second
			playerPosition = service.liveData.third

		}

		override fun onServiceDisconnected(name: ComponentName?) {
			uiInteractions = null
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {

		obweiViewModel

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_unique)

		val intent = Intent(this, MusicService::class.java)
		startService(intent)
		bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)

		navMenu.setOnNavigationItemReselectedListener {}
		navMenu.setOnNavigationItemSelectedListener(this)

	}
	override fun onBackPressed() {
		val f = supportFragmentManager.findFragmentByTag(playerFragmentTag)
		if (f?.isVisible == true) closePlayer()
		else super.onBackPressed()
	}

	override fun onNavigationItemSelected(p0: MenuItem): Boolean {

		val navFragment = when (p0.itemId) {
			R.id.songsMenuOption -> SongsNavFragment()
			R.id.albumsMenuOption -> AlbumsNavFragment()
			R.id.artistsMenuOption -> ArtistsNavFragment()
			else -> return false
		}

		supportFragmentManager.beginTransaction()
			.replace(R.id.navHost, navFragment, navFragmentsKey)
			.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
			.commit()

		return true

	}

	override fun openPlayer() {

		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag)
		val transaction = supportFragmentManager.beginTransaction()

		if (fragment == null) {
			val newFragment = PlayerFragment.newInstance(serviceState, queue, playerPosition)
			transaction.add(R.id.topContainer, newFragment, playerFragmentTag)
		} else {
			transaction.show(fragment)
		}
		transaction.commit()

	}
	override fun closePlayer() {

		val fragment = supportFragmentManager.findFragmentByTag(playerFragmentTag) ?: throw IllegalStateException()

		supportFragmentManager.beginTransaction().hide(fragment).commit()

	}

}
