package io.github.vinceglb.sample.core.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

fun main() = application {
	FileKit.init("FileKitSampleCore")
	Window(onCloseRequest = ::exitApplication, title = "Picker Core Sample") {
		val platformSettings = FileKitDialogSettings(this.window)
		App(platformSettings)
	}
}
