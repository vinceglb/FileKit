package io.github.vinceglb.filekit

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

public actual object FileKit

public actual val FileKit.filesDir: PlatformFile
    get() = NSFileManager
        .defaultManager
        .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        .firstOrNull()
        ?.let { it as NSURL? }
        ?.let(::PlatformFile)
        ?: throw IllegalStateException("Could not find files directory")

public actual val FileKit.cacheDir: PlatformFile
    get() = NSFileManager
        .defaultManager
        .URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        .firstOrNull()
        ?.let { it as NSURL? }
        ?.let(::PlatformFile)
        ?: throw IllegalStateException("Could not find cache directory")
