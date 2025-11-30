@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.sample.explorer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init("File Explorer Sample")

    Window(
        onCloseRequest = ::exitApplication,
        title = "File Explorer",
    ) {
        App()
    }
}
