package io.github.vinceglb.filekit.dialog

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun <Out> FileKit.pickFile(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings? = null,
): Out?

// TODO to deprecate or keep for the helper? Create new function without bytes parameter? (if target supports it)
// TODO rename baseName to nameWithoutExtension?
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
