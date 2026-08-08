package io.github.vinceglb.filekit.sample.shared.ui.screens.sharefile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher as rememberFileKitShareLauncher

@Composable
internal actual fun rememberShareFileLauncher(
    onError: (FileKitDialogException) -> Unit,
): ShareFileLauncher {
    val launcher = rememberFileKitShareLauncher(onError = onError)

    return remember(launcher) {
        object : ShareFileLauncher {
            override val isSupported: Boolean = true

            override fun launch(files: List<PlatformFile>) {
                launcher.launch(files)
            }
        }
    }
}
