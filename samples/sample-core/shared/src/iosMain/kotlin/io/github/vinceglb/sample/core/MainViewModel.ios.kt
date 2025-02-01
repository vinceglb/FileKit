package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.takePhoto

actual suspend fun takePhotoIfSupported(): PlatformFile? {
    return FileKit.takePhoto()
}
