@file:Suppress("UNUSED_VARIABLE")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
private fun CompileCameraLauncherCallShapes() {
    val legacy = rememberCameraPickerLauncher { _: PlatformFile? -> }
    val explicit = rememberCameraPickerLauncher(
        onError = { _: FileKitDialogException -> },
        onResult = { _: PlatformFile? -> },
    )
}
