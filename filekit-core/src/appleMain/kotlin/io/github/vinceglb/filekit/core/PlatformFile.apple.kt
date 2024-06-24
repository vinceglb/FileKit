package io.github.vinceglb.filekit.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.posix.memcpy

public actual data class PlatformFile(
    val nsUrl: NSURL,
) {
    public actual val name: String =
        nsUrl.lastPathComponent ?: ""

    public actual val path: String? =
        nsUrl.absoluteString

    @OptIn(ExperimentalForeignApi::class)
    public actual suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) {
        // Get the NSData from the NSURL
        val nsData = NSData.dataWithContentsOfURL(nsUrl)
            ?: throw IllegalStateException("Failed to read data from $nsUrl")

        val byteArraySize: Int =
            if (nsData.length > Int.MAX_VALUE.toUInt()) Int.MAX_VALUE else nsData.length.toInt()

        if (byteArraySize == 0) {
            return@withContext ByteArray(0)
        }

        ByteArray(byteArraySize).apply {
            usePinned {
                memcpy(it.addressOf(0), nsData.bytes, nsData.length)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    public actual fun getSize(): Long? {
        memScoped {
            val valuePointer: CPointer<ObjCObjectVar<Any?>> = alloc<ObjCObjectVar<Any?>>().ptr
            val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
                alloc<ObjCObjectVar<NSError?>>().ptr
            nsUrl.getResourceValue(valuePointer, NSURLFileSizeKey, errorPointer)
            return valuePointer.pointed.value as? Long?
        }
    }
}

public actual data class PlatformDirectory(
    val nsUrl: NSURL,
) {
    public actual val path: String? =
        nsUrl.absoluteString
}
