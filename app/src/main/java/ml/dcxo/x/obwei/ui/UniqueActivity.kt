package ml.dcxo.x.obwei.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.unique_activity.*
import ml.dcxo.x.obwei.viewModel.ObweiViewModel
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseNavFragment
import ml.dcxo.x.obwei.ui.fragments.nav.*
import ml.dcxo.x.obwei.utils.navFragmentsKey

class UniqueActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

	val obweiViewModel: ObweiViewModel by lazy { ViewModelProviders.of(this).get(ObweiViewModel::class.java) }

	override fun onCreate(savedInstanceState: Bundle?) {

		obweiViewModel

		super.onCreate(savedInstanceState)
		setContentView(R.layout.unique_activity)

		navMenu.setOnNavigationItemReselectedListener {}
		navMenu.setOnNavigationItemSelectedListener(this)

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
			.commit()

		return true

	}

}
