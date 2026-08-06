package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import kotlinx.coroutines.launch

/**
 * Creates and remembers a [PhotoResultLauncher] for taking a picture or video with the camera.
 *
 * @param openCameraSettings Platform-specific settings for the camera.
 * @param onResult Callback invoked with the saved file, or null if cancelled.
 * @return A [PhotoResultLauncher] that can be used to launch the camera.
 */
@Composable
public actual fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher = rememberCameraPickerLauncher(
    openCameraSettings = openCameraSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [PhotoResultLauncher] for taking a picture or video with the camera.
 *
 * @param openCameraSettings Platform-specific settings for the camera.
 * @param onError Callback invoked when a valid camera operation cannot start or complete. It is not invoked for user
 * dismissal, coroutine cancellation, invalid invocations, or unexpected defects.
 * @param onResult Callback invoked with the saved file, or null if dismissed.
 * @return A [PhotoResultLauncher] that can be used to launch the camera.
 */
@Composable
public actual fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    // Coroutine
    val coroutineScope = rememberCoroutineScope()
    val stableOpenCameraSettings = rememberStableOpenCameraSettings(openCameraSettings)

    // Updated state
    val currentOpenCameraSettings by rememberUpdatedState(stableOpenCameraSettings)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnResult by rememberUpdatedState(onResult)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        PhotoResultLauncher { type, cameraFacing, destinationFile ->
            coroutineScope.launch {
                runCameraPickerLauncher(
                    openCameraPicker = {
                        fileKit.openCameraPicker(
                            type = type,
                            cameraFacing = cameraFacing,
                            destinationFile = destinationFile,
                            openCameraSettings = currentOpenCameraSettings,
                        )
                    },
                    onError = currentOnError,
                    onResult = currentOnResult,
                )
            }
        }
    }

    return returnedLauncher
}
