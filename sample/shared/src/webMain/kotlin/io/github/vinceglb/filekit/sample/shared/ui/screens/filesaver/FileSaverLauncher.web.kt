package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
internal actual fun rememberFileSaverLauncher(
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher = remember {
    object : FileSaverLauncher {
        override val isSupported: Boolean = false

        override fun launch(
            suggestedName: String,
            defaultExtension: String?,
            allowedExtensions: Set<String>?,
            directory: PlatformFile?,
        ) {
            onResult(null)
        }
    }
}
