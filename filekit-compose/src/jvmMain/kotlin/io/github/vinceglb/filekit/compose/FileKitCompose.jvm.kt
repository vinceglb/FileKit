package io.github.vinceglb.filekit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.IPlatformFile
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType

@Composable
public fun <Out> WindowScope.rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        mode = mode,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = FileKitPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (IPlatformFile?) -> Unit,
): PickerResultLauncher {
    return rememberFilePickerLauncher(
        type = type,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = FileKitPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    onResult: (IPlatformFile?) -> Unit,
): PickerResultLauncher {
    return rememberDirectoryPickerLauncher(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = FileKitPlatformSettings(this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    onResult: (IPlatformFile?, IPlatformFile?) -> Unit,
): SaverResultLauncher {
    return rememberFileSaverLauncher(
        platformSettings = FileKitPlatformSettings(this.window),
        onResult = onResult,
    )
}
