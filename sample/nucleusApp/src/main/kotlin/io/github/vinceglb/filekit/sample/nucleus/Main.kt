package io.github.vinceglb.filekit.sample.nucleus

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.sample.shared.App

fun main() = nucleusApplication(backend = NucleusBackend.Tao) {
    FileKit.init(appId = "io.github.vinceglb.filekit.sample.nucleus")

    val windowState = rememberWindowState(size = DpSize(width = 920.dp, height = 720.dp))

    DecoratedWindow(
        state = windowState,
        title = "FileKit Nucleus Sample",
        onCloseRequest = ::exitApplication,
    ) {
        App()
    }
}
