@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.filekit.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init(appId = "io.github.vinceglb.filekit.sample")

    Window(
        onCloseRequest = ::exitApplication,
        title = "FileKit Sample",
    ) {
        // Configure macOS window appearance
        window.apply {
            rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }

        App()
    }
}
