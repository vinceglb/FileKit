package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

/**
 * Opens a file picker dialog.
 *
 * @param type The type of files to pick (e.g. Images, Videos, or specific extensions). Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The result of the picker, depending on the [mode].
 * @throws FileKitPickerException When the user selected files but FileKit could not resolve them.
 */
public suspend fun <A, B> FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<A, B>,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): A {
    val flow = platformOpenFilePicker(
        type = type,
        mode = mode.getPickerMode(),
        directory = directory,
        dialogSettings = dialogSettings,
    )
    return mode.parseResult(flow)
}

/**
 * Opens a file picker dialog in single selection mode.
 *
 * @param type The type of files to pick (e.g. Images, Videos, or specific extensions). Defaults to [FileKitType.File].
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked [PlatformFile], or null if cancelled.
 * @throws FileKitPickerException When the user selected a file but FileKit could not resolve it.
 */
public suspend fun FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? = openFilePicker(
    type = type,
    mode = FileKitMode.Single,
    directory = directory,
    dialogSettings = dialogSettings,
)

internal expect suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>>

/**
 * Opens a directory picker dialog.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked directory as a [PlatformFile], or null if cancelled.
 */
public expect suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?
