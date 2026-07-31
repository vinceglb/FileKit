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
    onError = legacyNullResultOnDialogFailure(onResult),
    onResult = onResult,
)

@Composable
public actual fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher {
    // Coroutine
    val coroutineScope = rememberCoroutineScope()
    val pendingState = remember { LauncherPendingState("camera picker") }
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
            coroutineScope.launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
                runDialogLauncher(
                    openDialog = {
                        fileKit.openCameraPicker(
                            type = type,
                            cameraFacing = cameraFacing,
                            destinationFile = destinationFile,
                            openCameraSettings = currentOpenCameraSettings,
                        )
                    },
                    beforeCallback = finishPendingLaunch,
                    onError = currentOnError,
                    onResult = currentOnResult,
                )
            }
        }
    }

    return returnedLauncher
}
