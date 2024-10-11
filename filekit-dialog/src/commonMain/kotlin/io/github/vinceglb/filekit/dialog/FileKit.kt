package io.github.vinceglb.filekit.dialog

import io.github.vinceglb.filekit.PlatformFile

public expect object FileKit

public expect suspend fun <Out> FileKit.pickFile(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
): Out?

public expect suspend fun FileKit.saveFile(
    bytes: ByteArray? = null,
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
): PlatformFile?

public suspend fun FileKit.pickFile(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
): PlatformFile? {
    return pickFile(
        type = type,
        mode = PickerMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
}
