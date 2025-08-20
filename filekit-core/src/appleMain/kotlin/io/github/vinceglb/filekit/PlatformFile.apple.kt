package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toKotlinxPath
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringGetCString
import platform.CoreFoundation.CFStringGetLength
import platform.CoreFoundation.CFStringGetMaximumSizeForEncoding
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.UTTypeCreatePreferredIdentifierForTag
import platform.CoreServices.kUTTagClassFilenameExtension
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.NSError
import platform.Foundation.NSURL

public actual data class PlatformFile(
    val nsUrl: NSURL,
) {
    public actual override fun toString(): String = path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformFile) return false
        if (nsUrl.path != other.nsUrl.path) return false
        return true
    }

    override fun hashCode(): Int {
        return nsUrl.path.hashCode()
    }

    public actual companion object
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL(string = path.toString()))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    nsUrl.toKotlinxPath()

public actual val PlatformFile.extension: String
    get() = nsUrl.pathExtension ?: ""

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

public actual fun PlatformFile.absolutePath(): String =
    nsUrl.absoluteString ?: ""

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(NSURL.fileURLWithPath(it.toString())) }
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(NSURL.fileURLWithPath(it.toString())) }
    }

@OptIn(ExperimentalForeignApi::class)
public actual fun PlatformFile.mimeType(): MimeType? = withScopedAccess {
    if (extension.isBlank()) {
        return null
    }

    memScoped {
        val cfExtension: CFStringRef? = CFStringCreateWithCString(
            alloc = kCFAllocatorDefault,
            cStr = extension.lowercase(),
            encoding = kCFStringEncodingUTF8
        )

        if (cfExtension == null) {
            return null
        }

        val utiRef: CFStringRef? = UTTypeCreatePreferredIdentifierForTag(
            inTagClass = kUTTagClassFilenameExtension,
            inTag = cfExtension,
            inConformingToUTI = null
        )

        CFRelease(cfExtension)

        if (utiRef == null) {
            return null
        }

        val mimeRef: CFStringRef? = UTTypeCopyPreferredTagWithClass(
            inUTI = utiRef,
            inTagClass = kUTTagClassMIMEType
        )

        CFRelease(utiRef)

        if (mimeRef == null) {
            return null
        }

        val mime = cfStringToKString(mimeRef)

        CFRelease(mimeRef)

        return mime?.let(MimeType::parse)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun cfStringToKString(cfString: CFStringRef?): String? {
    if (cfString == null) {
        return null
    }

    val length = CFStringGetLength(cfString)

    val maxSize = CFStringGetMaximumSizeForEncoding(
        length = length,
        encoding = kCFStringEncodingUTF8
    ) + 1

    return memScoped {
        val buffer = allocArray<ByteVar>(maxSize.toInt())

        if (
            CFStringGetCString(
                theString = cfString,
                buffer = buffer,
                bufferSize = maxSize,
                encoding = kCFStringEncodingUTF8
            )
        ) {
            buffer.toKString()
        } else null
    }
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean =
    nsUrl.startAccessingSecurityScopedResource()

public actual fun PlatformFile.stopAccessingSecurityScopedResource(): Unit =
    nsUrl.stopAccessingSecurityScopedResource()

@OptIn(ExperimentalForeignApi::class)
public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    withScopedAccess {
        val bookmarkData = nsUrl.bookmarkDataWithOptions(
            options = 0u,
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = null
        ) ?: throw FileKitException("Failed to create bookmark data")
        BookmarkData(bookmarkData.toByteArray())
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData
): PlatformFile = memScoped {
    val nsData = bookmarkData.bytes.toNSData()
    val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr

    val restoredUrl = NSURL.URLByResolvingBookmarkData(
        bookmarkData = nsData,
        options = 0u,
        relativeToURL = null,
        bookmarkDataIsStale = null,
        error = error
    ) ?: throw FileKitException("Failed to resolve bookmark data: ${error.pointed.value}")

    PlatformFile(restoredUrl)
}
