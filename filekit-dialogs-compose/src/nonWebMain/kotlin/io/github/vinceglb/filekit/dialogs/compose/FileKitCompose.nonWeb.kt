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
 * Dialog failures are reported as `onResult(null)` by this compatibility overload.
 * Use the overload with `onError` to distinguish failure from cancellation.
 */
@Composable
public fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher = rememberFileSaverLauncher(
    dialogSettings = dialogSettings,
    onError = legacyNullResultOnDialogFailure(onResult),
    onResult = onResult,
)

/**
 * Creates and remembers a [SaverResultLauncher] for saving a file.
 *
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when the file saver fails.
 * @param onResult Callback invoked with the saved file path, or null if cancelled.
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
