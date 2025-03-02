package io.github.vinceglb.filekit.dialogs.deprecated

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.download

@Deprecated(
    message = "Download the file using FileKit.download() instead. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
    replaceWith = ReplaceWith("FileKit.download(bytes = bytes, fileName = \"\$suggestedName.\$extension\")"),
)
public actual suspend fun FileKit.openFileSaver(
    bytes: ByteArray?,
    suggestedName: String,
    extension: String,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings
): PlatformFile? {
    if (bytes == null) {
        throw IllegalArgumentException("bytes must not be null")
    }

    FileKit.download(bytes = bytes, fileName = "$suggestedName.$extension")

    return null
}
