package io.github.vinceglb.sample.core.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.vinceglb.picker.core.Picker

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Important! Initialize the picker
		Picker.init(this)

		setContent {
			App()
		}
	}
}
