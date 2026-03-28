@file:Suppress("ktlint:compose:param-order-check")

package io.github.vinceglb.filekit.dialogs.compose

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
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
 */
@Composable
public fun <PickerResult, ConsumedResult> rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher {
    // Init FileKit
    InitFileKit()
    return rememberPlatformFilePickerLauncher(
        type = type,
        mode = mode,
        directory = directory,
        dialogSettings = dialogSettings,
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
 */
@Composable
public fun rememberFilePickerLauncher(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher = rememberFilePickerLauncher(
    type = type,
    mode = FileKitMode.Single,
    directory = directory,
    dialogSettings = dialogSettings,
    onResult = onResult,
)

@Composable
internal expect fun InitFileKit()

@Composable
internal expect fun <PickerResult, ConsumedResult> rememberPlatformFilePickerLauncher(
    type: FileKitType,
    mode: FileKitMode<PickerResult, ConsumedResult>,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
    onResult: (ConsumedResult) -> Unit,
): PickerResultLauncher

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
