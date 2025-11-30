package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

/**
 * Opens a file picker dialog.
 *
 * @param type The type of files to pick (e.g. Images, Videos, or specific extensions). Defaults to [FileKitType.File].
 * @param mode The picking mode (e.g. Single, Multiple).
 * @param title The title of the dialog. Supported on desktop platforms.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The result of the picker, depending on the [mode].
 */
public suspend fun <A, B> FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<A, B>,
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): A {
    val flow = platformOpenFilePicker(
        type = type,
        mode = mode.getPickerMode(),
        title = title,
        directory = directory,
        dialogSettings = dialogSettings,
    )
    return mode.parseResult(flow)
}

/**
 * Opens a file picker dialog in single selection mode.
 *
 * @param type The type of files to pick (e.g. Images, Videos, or specific extensions). Defaults to [FileKitType.File].
 * @param title The title of the dialog. Supported on desktop platforms.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked [PlatformFile], or null if cancelled.
 */
public suspend fun FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? = openFilePicker(
    type = type,
    mode = FileKitMode.Single,
    title = title,
    directory = directory,
    dialogSettings = dialogSettings,
)

internal expect suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>>
