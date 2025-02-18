package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.CompressFormat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.saveImageToGallery

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitDialogSettings): PlatformFile? {
    return FileKit.openDirectoryPicker(platformSettings = platformSettings)
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
