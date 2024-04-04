package io.github.vinceglb.sample.core

import platform.Foundation.NSDownloadsDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun downloadDirectoryPath(): String? {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDownloadsDirectory, NSUserDomainMask)
    val url = urls.firstOrNull() as? NSURL
    return url?.path
}
