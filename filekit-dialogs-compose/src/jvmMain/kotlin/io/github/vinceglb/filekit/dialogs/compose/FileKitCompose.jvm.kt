package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import java.awt.Window

@Composable
public fun <Out> WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<Out>,
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (Out?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
        type = type,
        mode = mode,
        title = title,
        directory = directory,
        dialogSettings = injectDialogSettings(dialogSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher(
        type = type,
        title = title,
        directory = directory,
        dialogSettings = injectDialogSettings(dialogSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberDirectoryPickerLauncher(
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher(
        title = title,
        directory = directory,
        dialogSettings = injectDialogSettings(dialogSettings, this.window),
        onResult = onResult,
    )
}

@Composable
public fun WindowScope.rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings? = null,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher {
    return io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher(
        dialogSettings = injectDialogSettings(dialogSettings, this.window),
        onResult = onResult,
    )
}

@Composable
internal actual fun InitFileKit() {}

private fun injectDialogSettings(
    dialogSettings: FileKitDialogSettings?,
    window: Window,
): FileKitDialogSettings {
    return dialogSettings
        ?.copy(parentWindow = window)
        ?: FileKitDialogSettings(parentWindow = window)
}
