package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import kotlinx.coroutines.launch

@Composable
public actual fun rememberCameraPickerLauncher(
    cameraFacing: FileKitCameraFacing,
    openCameraSettings: FileKitOpenCameraSettings,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    // Init FileKit
    InitFileKit()

    // Coroutine
    val coroutineScope = rememberCoroutineScope()

    // Updated state
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    // FIXME: Add openCameraSettings as a key to remember().
    //  To support this safely, openCameraSettings should be a data class (or override equals() and hashCode())
    //  and annotated with @Immutable.
    //  Note: On iOS, openCameraSettings is currently an empty class and not actually used,
    //  so it's safe to temporarily skip it as a key.
    val returnedLauncher = remember(cameraFacing) {
        PhotoResultLauncher { type, destinationFile ->
            coroutineScope.launch {
                val result = fileKit.openCameraPicker(
                    type = type,
                    cameraFacing = cameraFacing,
                    destinationFile = destinationFile,
                    openCameraSettings = openCameraSettings,
                )
                currentOnResult(result)
            }
        }
    }

    return returnedLauncher
}
