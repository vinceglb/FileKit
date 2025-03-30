package io.github.vinceglb.sample.explorer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "File Explorer",
    ) {
        App()
    }
}