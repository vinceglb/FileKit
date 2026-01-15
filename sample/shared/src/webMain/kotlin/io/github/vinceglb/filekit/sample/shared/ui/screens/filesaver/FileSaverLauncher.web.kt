package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberFileSaverLauncher(
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher = remember {
    object : FileSaverLauncher {
        override val isSupported: Boolean = false

        override fun launch(
            suggestedName: String,
            extension: String?,
            directory: PlatformFile?,
        ) {
            onResult(null)
        }
    }
}
