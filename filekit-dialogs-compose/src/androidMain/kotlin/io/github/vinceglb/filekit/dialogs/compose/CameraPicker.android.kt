package io.github.vinceglb.filekit.dialogs.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.CustomTakePicture
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.path
import kotlin.let

@Composable
public actual fun rememberCameraPickerLauncher(
    cameraFacing: FileKitCameraFacing,
    openCameraSettings: FileKitOpenCameraSettings,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    val currentOnResult by rememberUpdatedState(onResult)
    var currentPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        CustomTakePicture(cameraFacing)
    ) { success ->
        if (success) {
            currentOnResult(currentPhotoPath?.let(::PlatformFile))
        } else {
            currentOnResult(null)
        }
    }

    val authority = openCameraSettings.authority
    val returnedLauncher = remember(authority) {
        PhotoResultLauncher { _, destinationFile ->
            val newPhotoPath = destinationFile.path
            currentPhotoPath = newPhotoPath
            val uri = destinationFile.toAndroidUri(authority)
            takePictureLauncher.launch(uri)
        }
    }
    return returnedLauncher
}
