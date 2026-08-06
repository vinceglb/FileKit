@file:Suppress("ktlint:compose:param-order-check")

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
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings
import io.github.vinceglb.filekit.dialogs.shareFile
import kotlinx.coroutines.launch

/**
 * Creates and remembers a camera launcher whose operational failures are ignored without logging.
 *
 * Use the overload with `onError` to observe failures. User dismissal and Android camera-permission denial remain
 * nullable [onResult] outcomes.
 */
@Composable
public expect fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher

/**
 * Creates and remembers a camera launcher with explicit operational-failure handling.
 *
 * @param openCameraSettings Platform-specific settings for the camera.
 * @param onError Callback invoked when a valid camera operation cannot start or complete. It is not invoked for user
 * dismissal, Android camera-permission denial, coroutine cancellation, invalid invocations, or unexpected defects.
 * @param onResult Callback invoked with the saved file, or null if dismissed or Android camera permission is denied.
 */
@Composable
public expect fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher

@Composable
public fun rememberShareFileLauncher(
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
): ShareResultLauncher {
    // Coroutine
    val coroutineScope = rememberCoroutineScope()
    val stableShareSettings = rememberStableShareSettings(shareSettings)
    val currentShareSettings by rememberUpdatedState(stableShareSettings)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        ShareResultLauncher { files ->
            coroutineScope.launch {
                fileKit.shareFile(files, currentShareSettings)
            }
        }
    }

    return returnedLauncher
}
