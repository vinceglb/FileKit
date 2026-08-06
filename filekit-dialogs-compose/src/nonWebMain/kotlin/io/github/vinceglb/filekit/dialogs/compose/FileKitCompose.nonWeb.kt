package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

/**
 * Creates and remembers a [SaverResultLauncher] for saving a file.
 *
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the saved file path, or null if cancelled.
 * @return A [SaverResultLauncher] that can be used to launch the saver.
 *
 * Operational file-saver failures are ignored without logging by this compatibility overload.
 * Use the overload with `onError` to observe them. User cancellation remains an [onResult] value.
 */
@Composable
public fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher = rememberFileSaverLauncher(
    dialogSettings = dialogSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [SaverResultLauncher] for saving a file.
 *
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when a valid file-saving operation cannot complete. It is not invoked for user
 * cancellation, coroutine cancellation, invalid invocations, or unexpected defects.
 * @param onResult Callback invoked with the saved file path, or null if cancelled.
 * Exceptions thrown by [onError] or [onResult] propagate without a compensating callback.
 * @return A [SaverResultLauncher] that can be used to launch the saver.
 */
@Composable
public fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher = rememberPlatformFileSaverLauncher(
    dialogSettings = dialogSettings,
    onError = onError,
    onResult = onResult,
)

@Composable
internal expect fun rememberPlatformFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher
