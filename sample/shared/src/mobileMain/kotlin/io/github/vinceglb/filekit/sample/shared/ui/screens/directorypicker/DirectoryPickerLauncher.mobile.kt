package io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher as rememberFileKitDirectoryPickerLauncher

@Composable
internal actual fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    onResult: (PlatformFile?) -> Unit,
): DirectoryPickerLauncher {
    val launcher = rememberFileKitDirectoryPickerLauncher(
        directory = directory,
        onResult = onResult,
    )

    return remember(launcher) {
        object : DirectoryPickerLauncher {
            override val isSupported: Boolean = true

            override fun launch() {
                launcher.launch()
            }
        }
    }
}
