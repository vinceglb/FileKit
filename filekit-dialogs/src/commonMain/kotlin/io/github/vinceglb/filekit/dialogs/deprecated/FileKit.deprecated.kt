package io.github.vinceglb.filekit.dialogs.deprecated

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Deprecated(
    message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("saveFile(baseName, extension, directory, dialogSettings)"),
)
public expect suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    suggestedName: String = "file",
    extension: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile?
