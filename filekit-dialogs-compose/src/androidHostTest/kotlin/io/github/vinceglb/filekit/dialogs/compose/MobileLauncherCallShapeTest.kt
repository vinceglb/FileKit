package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
private fun CompileMobileLauncherCallShapes() {
    rememberCameraPickerLauncher(onResult = {})
    rememberCameraPickerLauncher {}
    rememberCameraPickerLauncher(
        onError = ::handleMobileDialogError,
        onResult = {},
    )
    rememberCameraPickerLauncher(onError = ::handleMobileDialogError) {}
    rememberShareFileLauncher()
    rememberShareFileLauncher(onError = ::handleMobileDialogError)
    rememberShareFileLauncher { error -> handleMobileDialogError(error) }
}

private fun handleMobileDialogError(error: FileKitDialogException) = Unit
