package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.shareFile

actual fun downloadDirectoryPath(): PlatformFile? =
    null

actual suspend fun takePhotoIfSupported(): PlatformFile? {
    return FileKit.openCameraPicker()
}

actual suspend fun shareFileIfSupported(file: PlatformFile) {
    FileKit.shareFile(file)
}
