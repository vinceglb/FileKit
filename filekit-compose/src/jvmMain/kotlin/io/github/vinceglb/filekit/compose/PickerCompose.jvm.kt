package io.github.vinceglb.filekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.core.PickerPlatformSettings
import io.github.vinceglb.filekit.core.PickerSelectionMode
import io.github.vinceglb.filekit.core.PickerSelectionType
import io.github.vinceglb.filekit.core.PlatformDirectory
import io.github.vinceglb.filekit.core.PlatformFile

@Composable
public fun <Out> WindowScope.rememberFilePickerLauncher(
    type: PickerSelectionType = PickerSelectionType.File(),
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        mode = mode,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: PickerSelectionType = PickerSelectionType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (PlatformDirectory?) -> Unit,
): PickerResultLauncher {
    return rememberDirectoryPickerLauncher(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    return rememberFileSaverLauncher(
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}
