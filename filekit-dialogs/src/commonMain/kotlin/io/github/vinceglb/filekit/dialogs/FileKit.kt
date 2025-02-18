package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun <Out> FileKit.openFilePicker(
    type: PickerType = PickerType.File(),
    mode: PickerMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): Out?

public suspend fun FileKit.openFilePicker(
    type: PickerType = PickerType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? {
    return openFilePicker(
        type = type,
        mode = PickerMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
}

// TODO to deprecate or keep for the helper? Create new function without bytes parameter? (if target supports it)
// TODO rename baseName to nameWithoutExtension?
@Deprecated(
    message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("saveFile(baseName, extension, initialDirectory, platformSettings)"),
)
public expect suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?
