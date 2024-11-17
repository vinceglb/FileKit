package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.CompressFormat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.pickDirectory
import io.github.vinceglb.filekit.saveImageToGallery

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitDialogSettings?): PlatformFile? {
    return FileKit.pickDirectory(platformSettings = platformSettings)
}

actual suspend fun compressImage(bytes: ByteArray) {
    FileKit.compressImage(
        imageData = bytes,
        maxWidth = 200,
        maxHeight = 200,
        quality = 80,
        compressFormat = CompressFormat.JPEG
    )?.let {
        FileKit.saveImageToGallery(it, "compressed.jpg")
    }
}
