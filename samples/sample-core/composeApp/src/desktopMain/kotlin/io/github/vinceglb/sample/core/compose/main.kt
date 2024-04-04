package io.github.vinceglb.sample.core.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
	Window(onCloseRequest = ::exitApplication, title = "Picker Core Sample") {
		App()
	}
}
