package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.write

public expect suspend fun FileKit.openDirectoryPicker(
    title: String? = null,
    initialDirectory: String? = null,            // TODO change to PlatformFile?
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

public expect suspend fun FileKit.openFileSaver(
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

@Deprecated(
    message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("saveFile(baseName, extension, initialDirectory, dialogSettings)"),
)
public actual suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    baseName: String,
    extension: String,
    initialDirectory: String?,
    dialogSettings: FileKitDialogSettings
): PlatformFile? {
    val file = FileKit.openFileSaver(
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        dialogSettings = dialogSettings,
    )

    if (file != null && bytes != null) {
        file.write(bytes)
    }

    return file
}
