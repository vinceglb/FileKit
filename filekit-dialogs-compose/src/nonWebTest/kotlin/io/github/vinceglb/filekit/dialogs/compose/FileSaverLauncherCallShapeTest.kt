package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
private fun CompileFileSaverLauncherCallShapes() {
    val dialogSettings = FileKitDialogSettings.createDefault()

    rememberFileSaverLauncher(
        dialogSettings = dialogSettings,
        onResult = {},
    )
    rememberFileSaverLauncher(dialogSettings) {}
    rememberFileSaverLauncher(
        dialogSettings = dialogSettings,
        onError = ::handleFileSaverError,
        onResult = {},
    )
    rememberFileSaverLauncher(dialogSettings, onError = ::handleFileSaverError) {}
}

private fun handleFileSaverError(error: FileKitDialogException) = Unit
