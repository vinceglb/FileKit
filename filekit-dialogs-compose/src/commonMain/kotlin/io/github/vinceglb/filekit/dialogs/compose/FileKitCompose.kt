@file:Suppress("ktlint:compose:param-order-check")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitType

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
 * Picker failures are ignored by this overload. Use the overload with `onError` to handle them.
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
 * @param onError Callback invoked when FileKit cannot resolve the selected files.
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
 * Picker failures are ignored by this overload. Use the overload with `onError` to handle them.
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
 * @param onError Callback invoked when FileKit cannot resolve the selected file.
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
    val result = try {
        openPicker()
    } catch (failure: FileKitPickerException) {
        onError(failure)
        return
    }
    mode.consumeResult(result, onResult)
}

/**
 * Creates and remembers a [PickerResultLauncher] for picking a directory.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @param onResult Callback invoked with the picked directory, or null if cancelled.
 * @return A [PickerResultLauncher] that can be used to launch the picker.
 */
@Composable
public expect fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher
