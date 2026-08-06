@file:Suppress("ktlint:compose:param-order-check")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Creates and remembers a [PickerResultLauncher] for picking files.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the result.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Operational picker failures are ignored without logging by this compatibility overload.
 * Use the overload with `onError` to observe them. User cancellation remains an [onResult] value.
 */
@Composable
public fun <PickerResult, ConsumedResult> rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    mode = mode,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [PickerResultLauncher] for picking files.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when a valid picker operation cannot complete. It is not invoked for user cancellation,
 * coroutine cancellation, invalid invocations, unexpected defects, or [io.github.vinceglb.filekit.dialogs.FileKitPickerState.Failed]
 * values delivered by state-tracking modes.
 * @param onResult Callback invoked with the result.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public fun <PickerResult, ConsumedResult> rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher {
    val stableDialogSettings = rememberStableDialogSettings(dialogSettings)
    return rememberPlatformFilePickerLauncher(
        type = type,
        mode = mode,
        directory = directory,
        dialogSettings = stableDialogSettings,
        onError = onError,
        onResult = onResult,
    )
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a single file.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked file, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Operational picker failures are ignored without logging by this compatibility overload.
 * Use the overload with `onError` to observe them. User cancellation remains an [onResult] value.
 */
@Composable
public fun rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = {},
    onResult = onResult,
)

/**
 * Creates and remembers a [PickerResultLauncher] for picking a single file.
 *
 * @param type The type of files to pick. Defaults to [FileKitType.File].
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when a valid picker operation cannot complete. It is not invoked for user cancellation,
 * coroutine cancellation, invalid invocations, or unexpected defects.
 * @param onResult Callback invoked with the picked file, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public fun rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitPickerException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    mode = FileKitMode.Single,
    directory = directory,
    dialogSettings = dialogSettings,
    onError = onError,
    onResult = onResult,
)

@Composable
internal expect fun <PickerResult, ConsumedResult> rememberPlatformFilePickerLauncher(
    type: FileKitType,
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher

internal suspend fun <PickerResult, ConsumedResult> runFilePickerLauncher(
    mode: FileKitMode<PickerResult, ConsumedResult>,
    openPicker: suspend () -> PickerResult,
    onError: (FileKitPickerException) -> Unit,
    onResult: (ConsumedResult) -> Unit,
) {
    runDialogOperation(
        operation = openPicker,
        onError = { failure ->
            when (failure) {
                is FileKitPickerException -> onError(failure)
                else -> throw failure
            }
        },
        onResult = { result ->
            mode.consumePickerResult(result, onError, onResult)
        },
    )
}

private suspend fun <PickerResult, ConsumedResult> FileKitMode<PickerResult, ConsumedResult>.consumePickerResult(
    result: PickerResult,
    onFailure: (FileKitPickerException) -> Unit,
    onConsumed: (ConsumedResult) -> Unit,
) {
    when (this) {
        FileKitMode.Single,
        is FileKitMode.Multiple,
        -> {
            consumeResult(result, onConsumed)
        }

        FileKitMode.SingleWithState,
        is FileKitMode.MultipleWithState,
        -> {
            @Suppress("UNCHECKED_CAST")
            (result as Flow<ConsumedResult>)
                .catch { failure ->
                    when (failure) {
                        is FileKitPickerException -> onFailure(failure)
                        else -> throw failure
                    }
                }.collect(onConsumed)
        }
    }
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 *
 * Operational directory-picker failures are ignored without logging by this compatibility overload.
 * Use the overload with `onError` to observe them. User cancellation remains an [onResult] value.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onError Callback invoked when a valid directory operation cannot complete. It is not invoked for user cancellation,
 * coroutine cancellation, invalid invocations, or unexpected defects.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher

internal suspend fun runDirectoryPickerLauncher(
    openDirectoryPicker: suspend () -> PlatformFile?,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
) {
    runDialogOperation(
        operation = openDirectoryPicker,
        onError = onError,
        onResult = onResult,
    )
}
