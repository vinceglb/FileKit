@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.sample.core.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

fun main() = application {
    FileKit.init(appId = "FileKitSampleCore")
    Window(onCloseRequest = ::exitApplication, title = "Picker Core Sample") {
        val dialogSettings = FileKitDialogSettings(this.window)
        App(dialogSettings)
    }
}
