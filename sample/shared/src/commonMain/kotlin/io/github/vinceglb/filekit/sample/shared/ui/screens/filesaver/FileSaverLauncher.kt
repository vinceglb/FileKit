package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

internal interface FileSaverLauncher {
    val isSupported: Boolean

    fun launch(
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
    )
}

@Composable
internal expect fun rememberFileSaverLauncher(
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher
