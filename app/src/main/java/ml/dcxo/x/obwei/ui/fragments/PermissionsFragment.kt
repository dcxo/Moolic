package ml.dcxo.x.obwei.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import ml.dcxo.x.obwei.base.BaseFragment
import ml.dcxo.x.obwei.R

/**
 * Created by David on 14/02/2019 for ObweiX
 */
class PermissionsFragment: BaseFragment() {

	var permissionsGranted = false

	override val layoutInflated: Int = R.layout.fragment_permissions

	override fun editOnCreateView(view: View) {

		view.setOnClickListener {
			val permissionsNeeded = arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE )
			requestPermissions(permissionsNeeded, 0xCD)
		}

	}
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

		permissionsGranted = true
		if (requestCode == 0xCD) {
			for ((index, _) in permissions.withIndex()) {
				permissionsGranted = permissionsGranted && grantResults[index] == PackageManager.PERMISSION_GRANTED
			}
		}

		mActivity?.permissionsGranted = permissionsGranted
		mActivity?.handlePermissions(permissionsGranted)

		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

}