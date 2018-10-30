package ml.dcxo.x.obwei

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders

class UniqueActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {

		Obwei.obweiViewModel = ViewModelProviders.of(this).get(ObweiViewModel::class.java)

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_unique)
	}
}
