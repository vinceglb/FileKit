package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile

actual fun downloadDirectoryPath(): String? {
    val home = System.getProperty("user.home")
    return "$home/Downloads"
}

actual suspend fun takePhotoIfSupported(): PlatformFile? = null
