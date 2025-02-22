package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openCameraPicker

actual suspend fun takePhotoIfSupported(): PlatformFile? {
    return FileKit.openCameraPicker()
}
