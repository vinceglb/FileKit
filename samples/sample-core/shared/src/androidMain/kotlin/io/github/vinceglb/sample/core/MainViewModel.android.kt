package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.takePhoto

actual fun downloadDirectoryPath(): String? =
    null

actual suspend fun takePhotoIfSupported(): PlatformFile? {
    return FileKit.takePhoto()
}
