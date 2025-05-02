package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.saveImageToGallery
import io.github.vinceglb.filekit.write

actual suspend fun pickDirectoryIfSupported(dialogSettings: FileKitDialogSettings): PlatformFile? {
    return FileKit.openDirectoryPicker(dialogSettings = dialogSettings)
}

actual suspend fun compressImage(bytes: ByteArray) {
    val compressedImage = FileKit.compressImage(
        bytes = bytes,
        maxWidth = 200,
        maxHeight = 200,
        quality = 80,
        imageFormat = ImageFormat.JPEG
    )

    FileKit.saveImageToGallery(compressedImage, "compressed.jpg")
}

actual suspend fun saveFileOrDownload(
    file: PlatformFile,
    dialogSettings: FileKitDialogSettings
): PlatformFile? {
    val newFile = FileKit.openFileSaver(
        suggestedName = file.name,
        extension = file.extension,
        directory = file.parent(),
        dialogSettings = dialogSettings,
    )

    newFile?.let {
        newFile.write(file)
    }

    return newFile
}
