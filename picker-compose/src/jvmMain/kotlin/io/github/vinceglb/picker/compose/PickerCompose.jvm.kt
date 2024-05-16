package io.github.vinceglb.picker.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import io.github.vinceglb.picker.core.PickerPlatformSettings
import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.PickerSelectionType
import io.github.vinceglb.picker.core.PlatformDirectory
import io.github.vinceglb.picker.core.PlatformFile

@Composable
public fun <Out> FrameWindowScope.rememberFilePickerLauncher(
    type: PickerSelectionType = PickerSelectionType.File(),
    mode: PickerSelectionMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
) {
    rememberFilePickerLauncher(
        type = type,
        mode = mode,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun FrameWindowScope.rememberFilePickerLauncher(
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
public fun FrameWindowScope.rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (PlatformDirectory?) -> Unit,
) {
    rememberDirectoryPickerLauncher(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun FrameWindowScope.rememberFileSaverLauncher(
    onResult: (PlatformFile?) -> Unit,
) {
    rememberFileSaverLauncher(
        platformSettings = PickerPlatformSettings(this.window),
        onResult = onResult,
    )
}
