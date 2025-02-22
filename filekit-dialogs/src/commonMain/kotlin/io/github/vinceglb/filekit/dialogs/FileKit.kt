package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<Out>,
    title: String? = null,
    initialDirectory: String? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): Out?

public suspend fun FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    title: String? = null,
    initialDirectory: String? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? {
    return openFilePicker(
        type = type,
        mode = FileKitMode.Single,
        title = title,
        initialDirectory = initialDirectory,
        dialogSettings = dialogSettings,
    )
}

// TODO to deprecate or keep for the helper? Create new function without bytes parameter? (if target supports it)
// TODO rename baseName to nameWithoutExtension?
@Deprecated(
    message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("saveFile(baseName, extension, initialDirectory, dialogSettings)"),
)
public expect suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?
