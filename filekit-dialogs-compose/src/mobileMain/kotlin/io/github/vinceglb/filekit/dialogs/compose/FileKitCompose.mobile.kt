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

/**
 * Creates a camera launcher whose result is null for cancellation, permission denial, or a dialog failure.
 * Use the overload with `onError` to distinguish dialog failure from cancellation.
 */
@Composable
public expect fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher

/** Creates a camera launcher with an explicit dialog-failure callback. */
@Composable
public expect fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher

/**
 * Creates a share launcher that ignores dialog failures.
 * Use the overload with `onError` to observe them.
 */
@Composable
public fun rememberShareFileLauncher(
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
): ShareResultLauncher = rememberShareFileLauncher(
    shareSettings = shareSettings,
    onError = legacyIgnoreDialogFailure(),
)

/** Creates a share launcher with an explicit dialog-failure callback. */
@Composable
public fun rememberShareFileLauncher(
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
): ShareResultLauncher {
    // Coroutine
    val coroutineScope = rememberCoroutineScope()
    val pendingState = remember { LauncherPendingState("share sheet") }
    val stableShareSettings = rememberStableShareSettings(shareSettings)
    val currentShareSettings by rememberUpdatedState(stableShareSettings)
    val currentOnError by rememberUpdatedState(onError)

    // FileKit
    val fileKit = remember { FileKit }

    // FileKit launcher
    val returnedLauncher = remember {
        ShareResultLauncher { files ->
            coroutineScope.launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
                runDialogLauncher(
                    openDialog = { fileKit.shareFile(files, currentShareSettings) },
                    beforeCallback = finishPendingLaunch,
                    onError = currentOnError,
                    onResult = {},
                )
            }
        }
    }

    return returnedLauncher
}
