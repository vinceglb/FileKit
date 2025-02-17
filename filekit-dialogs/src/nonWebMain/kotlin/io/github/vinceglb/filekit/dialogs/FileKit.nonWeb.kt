package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.write

public expect suspend fun FileKit.pickDirectory(
    title: String? = null,
    initialDirectory: String? = null,            // TODO change to PlatformFile?
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

public expect suspend fun FileKit.saveFile(
    baseName: String = "file",
    extension: String,
    initialDirectory: String? = null,
    platformSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?

@Deprecated(
    message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("saveFile(baseName, extension, initialDirectory, platformSettings)"),
)
public actual suspend fun FileKit.saveFile(
    bytes: ByteArray?,
    baseName: String,
    extension: String,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings
): PlatformFile? {
    val file = FileKit.saveFile(
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )

    if (file != null && bytes != null) {
        file.write(bytes)
    }

    return file
}
