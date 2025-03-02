package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<Out>,
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): Out?

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
