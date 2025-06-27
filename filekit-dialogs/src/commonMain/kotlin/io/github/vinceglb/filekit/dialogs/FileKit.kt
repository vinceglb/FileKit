package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

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

public suspend fun FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? {
    return openFilePicker(
        type = type,
        mode = FileKitMode.Single,
        title = title,
        directory = directory,
        dialogSettings = dialogSettings,
    )
}

internal expect suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>>
