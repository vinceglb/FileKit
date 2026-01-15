package io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

internal interface DirectoryPickerLauncher {
    val isSupported: Boolean

    fun launch()
}

@Composable
internal expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile?,
    onResult: (PlatformFile?) -> Unit,
): DirectoryPickerLauncher
