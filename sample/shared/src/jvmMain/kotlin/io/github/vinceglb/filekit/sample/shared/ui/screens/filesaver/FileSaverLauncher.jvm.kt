package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher as rememberFileKitSaverLauncher

@Composable
internal actual fun rememberFileSaverLauncher(
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher {
    val launcher = rememberFileKitSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = onError,
        onResult = onResult,
    )

    return remember(launcher) {
        object : FileSaverLauncher {
            override val isSupported: Boolean = true

            override fun launch(
                suggestedName: String,
                defaultExtension: String?,
                allowedExtensions: Set<String>?,
                directory: PlatformFile?,
            ) {
                launcher.launch(
                    suggestedName = suggestedName,
                    defaultExtension = defaultExtension,
                    allowedExtensions = allowedExtensions,
                    directory = directory,
                )
            }
        }
    }
}
