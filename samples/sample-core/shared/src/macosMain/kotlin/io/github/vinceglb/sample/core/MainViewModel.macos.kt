package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile

actual suspend fun takePhotoIfSupported(): PlatformFile? = null

actual suspend fun shareFileIfSupported(file: PlatformFile) {

}