package ml.dcxo.x.obwei.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomdialog.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.service.UIInteractions
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 03/11/2018 for ObweiX
 */
class SongBottomDialog: BottomSheetDialogFragment() {

	lateinit var uiInteractions: UIInteractions
	lateinit var song: Song

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

		dialog.window?.setBackgroundDrawable( ColorDrawable(Color.RED) )
		val v = inflater.inflate(R.layout.bottomdialog, container, false)

		v.addToQueueOption.setOnClickListener {
			uiInteractions.onAddToQueue(song)
			dialog.cancel()
		}

		return v

	}

	override fun onDestroyView() {

		view!!.addToQueueOption.setOnClickListener(null)

		super.onDestroyView()
	}

	companion object {

		fun newInstance(song: Song, uiInteractions: UIInteractions) = SongBottomDialog().apply {
			this.song = song
			this.uiInteractions = uiInteractions
		}

	}

}