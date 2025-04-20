package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

actual suspend fun pickDirectoryIfSupported(
    dialogSettings: FileKitDialogSettings
): PlatformFile? = null

actual fun downloadDirectoryPath(): PlatformFile? = null

actual suspend fun takePhotoIfSupported(): PlatformFile? = null

actual suspend fun shareFileIfSupported(file: PlatformFile) {

}

actual suspend fun compressImage(bytes: ByteArray) {}