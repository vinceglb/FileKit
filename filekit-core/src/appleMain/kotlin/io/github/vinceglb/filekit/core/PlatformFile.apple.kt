package io.github.vinceglb.filekit.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.NSError
import platform.Foundation.NSInputStream
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

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    public actual suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) {
        memScoped {
            // Start accessing the security scoped resource
            nsUrl.startAccessingSecurityScopedResource()

            // Read the data
            val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
            val nsData = NSData.dataWithContentsOfURL(nsUrl, NSDataReadingUncached, error)
                ?: throw IllegalStateException("Failed to read data from $nsUrl. Error: ${error.pointed.value}")

            // Stop accessing the security scoped resource
            nsUrl.stopAccessingSecurityScopedResource()

            // Copy the data to a ByteArray
            ByteArray(nsData.length.toInt()).apply {
                memcpy(this.refTo(0), nsData.bytes, nsData.length)
            }
        }
    }

    public actual fun getStream(): PlatformInputStream {
        return PlatformInputStream(NSInputStream(nsUrl))
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

    public actual fun supportsStreams(): Boolean = true
}

public actual data class PlatformDirectory(
    val nsUrl: NSURL,
) {
    public actual val path: String? =
        nsUrl.absoluteString
}
