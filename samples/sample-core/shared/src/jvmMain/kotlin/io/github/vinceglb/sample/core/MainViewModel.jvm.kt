package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.downloadDir

actual fun downloadDirectoryPath(): PlatformFile? = FileKit.downloadDir

actual suspend fun takePhotoIfSupported(): PlatformFile? = null

actual suspend fun shareFileIfSupported(file: PlatformFile) {

}
