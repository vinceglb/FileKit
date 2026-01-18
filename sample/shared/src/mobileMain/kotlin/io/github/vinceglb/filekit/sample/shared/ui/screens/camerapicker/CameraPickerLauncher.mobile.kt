package io.github.vinceglb.filekit.sample.shared.ui.screens.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher as rememberFileKitCameraPickerLauncher

@Composable
internal actual fun rememberCameraPickerLauncher(
    onResult: (PlatformFile?) -> Unit,
): CameraPickerLauncher {
    val launcher = rememberFileKitCameraPickerLauncher(onResult = onResult)

    return remember(launcher) {
        object : CameraPickerLauncher {
            override val isSupported: Boolean = true

            override fun launch(cameraFacing: CameraFacingOption) {
                launcher.launch(
                    cameraFacing = when (cameraFacing) {
                        CameraFacingOption.System -> FileKitCameraFacing.System
                        CameraFacingOption.Front -> FileKitCameraFacing.Front
                        CameraFacingOption.Back -> FileKitCameraFacing.Back
                    },
                )
            }
        }
    }
}
