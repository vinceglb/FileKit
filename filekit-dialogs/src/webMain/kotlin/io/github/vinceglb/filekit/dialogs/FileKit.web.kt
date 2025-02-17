package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download

@Deprecated(
    message = "Download the file using FileKit.download() instead. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("FileKit.download(bytes = bytes, fileName = \"\$baseName.\$extension\")"),
)
public actual suspend fun FileKit.saveFile(
    bytes: ByteArray?,
    baseName: String,
    extension: String,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings
): PlatformFile? {
    if (bytes == null) {
        throw IllegalArgumentException("bytes must not be null")
    }

    FileKit.download(bytes = bytes, fileName = "$baseName.$extension")

    return null
}
