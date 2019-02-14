package ml.dcxo.x.obwei

import android.app.Application
import ml.dcxo.x.obwei.viewModel.ObweiViewModel

/**
 * Created by David on 25/10/2018 for XOXO
 */
class Obwei: Application() {

	companion object {
		lateinit var obweiViewModel: ObweiViewModel
	}

}