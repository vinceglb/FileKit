package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.PickerMode
import io.github.vinceglb.filekit.dialogs.PickerType
import io.github.vinceglb.filekit.PlatformFile
import java.awt.Window

@Composable
public fun <Out> WindowScope.rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
        type = type,
        mode = mode,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = injectPlatformSettings(platformSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
        type = type,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = injectPlatformSettings(platformSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = injectPlatformSettings(platformSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    platformSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher(
        platformSettings = injectPlatformSettings(platformSettings, this.window),
        onResult = onResult,
    )
}

private fun injectPlatformSettings(
    platformSettings: FileKitDialogSettings?,
    window: Window,
): FileKitDialogSettings {
    return platformSettings
        ?.copy(parentWindow = window)
        ?: FileKitDialogSettings(parentWindow = window)
}
