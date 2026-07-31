package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

internal interface FileSaverLauncher {
    val isSupported: Boolean

    fun launch(
        suggestedName: String,
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
    )
}

@Composable
internal expect fun rememberFileSaverLauncher(
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher
