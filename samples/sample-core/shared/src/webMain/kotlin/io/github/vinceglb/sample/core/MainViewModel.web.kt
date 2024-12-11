package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings

actual suspend fun pickDirectoryIfSupported(
    platformSettings: FileKitDialogSettings
): PlatformFile? = null

actual fun downloadDirectoryPath(): String? = null

actual suspend fun takePhotoIfSupported(): PlatformFile? = null

actual suspend fun compressImage(bytes: ByteArray) {}