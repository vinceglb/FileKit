package io.github.vinceglb.sample.core

actual fun downloadDirectoryPath(): String? {
    val home = System.getProperty("user.home")
    return "$home/Downloads"
}
