package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.dialogs.shareImageFile

actual suspend fun takePhotoIfSupported(): PlatformFile? {
    return FileKit.openCameraPicker()
}

actual suspend fun shareImageIfSupported(file: PlatformFile) {
    FileKit.shareImageFile(file)
}