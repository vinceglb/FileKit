@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings

@Composable
private fun CompileIosCameraLauncherCallShapes() {
    val settings = FileKitOpenCameraSettings.createDefault()
    val legacy = rememberCameraPickerLauncher(
        openCameraSettings = settings,
        onResult = { _: PlatformFile? -> },
    )
    val explicit = rememberCameraPickerLauncher(
        openCameraSettings = settings,
        onError = { _: FileKitDialogException -> },
        onResult = { _: PlatformFile? -> },
    )
}
