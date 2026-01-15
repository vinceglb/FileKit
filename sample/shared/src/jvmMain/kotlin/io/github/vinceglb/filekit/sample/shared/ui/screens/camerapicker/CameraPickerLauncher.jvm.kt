package io.github.vinceglb.filekit.sample.shared.ui.screens.camerapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberCameraPickerLauncher(
    onResult: (PlatformFile?) -> Unit,
): CameraPickerLauncher = remember {
    object : CameraPickerLauncher {
        override val isSupported: Boolean = false

        override fun launch(cameraFacing: CameraFacingOption) {
            onResult(null)
        }
    }
}
