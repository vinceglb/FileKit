package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType

@Composable
private fun CompileCommonLauncherCallShapes() {
    rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = {},
    )
    rememberFilePickerLauncher(type = FileKitType.Image) {}
    rememberFilePickerLauncher(
        type = FileKitType.Image,
        onError = ::handlePickerError,
        onResult = {},
    )
    rememberFilePickerLauncher(
        type = FileKitType.Image,
        onError = ::handlePickerError,
    ) {}
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onResult = {},
    )
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
    ) {}
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onError = ::handlePickerError,
        onResult = {},
    )
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onError = ::handlePickerError,
    ) {}
    rememberDirectoryPickerLauncher(onResult = {})
    rememberDirectoryPickerLauncher {}
    rememberDirectoryPickerLauncher(
        onError = ::handleDialogError,
        onResult = {},
    )
    rememberDirectoryPickerLauncher(onError = ::handleDialogError) {}
}

private fun handlePickerError(error: FileKitDialogException) = Unit

private fun handleDialogError(error: FileKitDialogException) = Unit
