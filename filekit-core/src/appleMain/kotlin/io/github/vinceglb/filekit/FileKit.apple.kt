package io.github.vinceglb.filekit

import androidx.annotation.IntRange
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
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
        ?: throw FileKitException("Could not find files directory")

public actual val FileKit.cacheDir: PlatformFile
    get() = NSFileManager
        .defaultManager
        .URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        .firstOrNull()
        ?.let { it as NSURL? }
        ?.let(::PlatformFile)
        ?: throw FileKitException("Could not find cache directory")

public actual suspend fun FileKit.compressImage(
    bytes: ByteArray,
    @IntRange(from = 0, to = 100) quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
    compressFormat: CompressFormat,
): ByteArray? = withContext(Dispatchers.IO) {
    // Step 1: Decode the ByteArray to UIImage (iOS) or NSImage (macOS)
    val nsData = bytes.toNSData()

    // Step 2: Compress the UIImage
    val compressedData = compress(nsData, quality, maxWidth, maxHeight, compressFormat)

    // Step 3: Return the compressed image as ByteArray
    val res = compressedData?.toByteArray()

    res
}

internal expect fun compress(
    nsData: NSData,
    quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
    compressFormat: CompressFormat,
): NSData?
