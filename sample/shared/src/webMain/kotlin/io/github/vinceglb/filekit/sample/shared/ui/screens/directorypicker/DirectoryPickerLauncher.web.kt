package io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    onResult: (PlatformFile?) -> Unit,
): DirectoryPickerLauncher = remember {
    object : DirectoryPickerLauncher {
        override val isSupported: Boolean = false

        override fun launch() {
            onResult(null)
        }
    }
}
