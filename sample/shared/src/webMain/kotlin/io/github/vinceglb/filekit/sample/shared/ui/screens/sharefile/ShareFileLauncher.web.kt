package io.github.vinceglb.filekit.sample.shared.ui.screens.sharefile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
internal actual fun rememberShareFileLauncher(
    onError: (FileKitDialogException) -> Unit,
): ShareFileLauncher = remember {
    object : ShareFileLauncher {
        override val isSupported: Boolean = false

        override fun launch(files: List<PlatformFile>) = Unit
    }
}
