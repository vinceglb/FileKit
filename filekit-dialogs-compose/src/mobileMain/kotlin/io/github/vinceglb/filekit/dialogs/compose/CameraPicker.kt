package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings

@Composable
public expect fun rememberCameraPickerLauncher(
    cameraFacing: FileKitCameraFacing = FileKitCameraFacing.Back,
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher
