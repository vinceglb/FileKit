package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.openDirectoryPicker(
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

public expect suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

public expect fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings = FileKitOpenFileSettings.createDefault(),
)
