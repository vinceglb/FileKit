package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.PlatformFile
import platform.Foundation.NSDownloadsDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun downloadDirectoryPath(): PlatformFile? {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDownloadsDirectory, NSUserDomainMask)
    val url = urls.firstOrNull() as? NSURL
    return url?.path?.let(::PlatformFile)
}
