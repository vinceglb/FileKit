package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.WindowScope
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitMode

@Composable
private fun WindowScope.CompileJvmLauncherCallShapes() {
    rememberFilePickerLauncher(onResult = {})
    rememberFilePickerLauncher(
        onError = ::handleJvmPickerError,
        onResult = {},
    )
    rememberFilePickerLauncher(onError = ::handleJvmPickerError) {}
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onResult = {},
    )
    rememberFilePickerLauncher(mode = FileKitMode.Multiple()) {}
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onError = ::handleJvmPickerError,
        onResult = {},
    )
    rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        onError = ::handleJvmPickerError,
    ) {}
    rememberDirectoryPickerLauncher(onResult = {})
    rememberDirectoryPickerLauncher {}
    rememberDirectoryPickerLauncher(
        onError = ::handleJvmDialogError,
        onResult = {},
    )
    rememberDirectoryPickerLauncher(onError = ::handleJvmDialogError) {}
    rememberFileSaverLauncher(onResult = {})
    rememberFileSaverLauncher {}
    rememberFileSaverLauncher(
        onError = ::handleJvmDialogError,
        onResult = {},
    )
    rememberFileSaverLauncher(onError = ::handleJvmDialogError) {}
}

private fun handleJvmPickerError(error: io.github.vinceglb.filekit.dialogs.FileKitPickerException) = Unit

private fun handleJvmDialogError(error: FileKitDialogException) = Unit
