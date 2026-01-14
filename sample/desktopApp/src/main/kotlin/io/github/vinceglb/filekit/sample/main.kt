@file:Suppress("ktlint:standard:filename")

package io.github.vinceglb.filekit.sample

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.sample.shared.App

fun main() = application {
    // Setup FileKit
    FileKit.init(appId = "io.github.vinceglb.filekit.sample")

    // MacOS System Appearance
    System.setProperty("apple.awt.application.appearance", "system")

    val windowState = rememberWindowState(size = DpSize(width = 920.dp, height = 720.dp))

    Window(
        state = windowState,
        title = "FileKit Sample",
        onCloseRequest = ::exitApplication,
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
