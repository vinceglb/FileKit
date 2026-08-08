package io.github.vinceglb.filekit.sample.shared.ui.screens.camerapicker

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

internal enum class CameraFacingOption {
    System,
    Front,
    Back,
}

internal interface CameraPickerLauncher {
    val isSupported: Boolean

    fun launch(cameraFacing: CameraFacingOption)
}

@Composable
internal expect fun rememberCameraPickerLauncher(
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): CameraPickerLauncher
