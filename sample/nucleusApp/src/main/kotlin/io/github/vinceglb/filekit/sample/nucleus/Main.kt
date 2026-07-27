package io.github.vinceglb.filekit.sample.nucleus

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.NucleusWindow
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.core.runtime.Platform
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.sample.shared.App

fun main() = nucleusApplication(backend = NucleusBackend.Tao) {
    FileKit.init(appId = "io.github.vinceglb.filekit.sample.nucleus")

    val windowState = rememberWindowState(size = DpSize(width = 920.dp, height = 720.dp))

    DecoratedWindow(
        state = windowState,
        title = "FileKit Nucleus Sample",
        onCloseRequest = ::exitApplication,
    ) {
        val dialogParent = nucleusWindow.fileKitDialogParent()
        App(
            dialogSettingsTransform = { settings ->
                settings.copy(parent = dialogParent)
            },
        )
    }
}

private fun NucleusWindow.fileKitDialogParent(): FileKitDialogParent? = when (Platform.Current) {
    Platform.Windows -> {
        // Nucleus exposes an HWND here only on Windows. Its Tao API intentionally
        // returns 0 on Linux, where FileKit requires an X11 XID or portal token.
        unsafe.taoWindow
            ?.nativeHandle
            ?.takeIf { it != 0L }
            ?.let(FileKitDialogParent::windows)
    }

    else -> {
        null
    }
}
