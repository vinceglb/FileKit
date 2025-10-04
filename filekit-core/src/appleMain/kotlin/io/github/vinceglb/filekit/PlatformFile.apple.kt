package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.div
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
import kotlinx.serialization.Serializable
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringGetCString
import platform.CoreFoundation.CFStringGetLength
import platform.CoreFoundation.CFStringGetMaximumSizeForEncoding
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLContentModificationDateKey
import platform.Foundation.NSURLContentTypeKey
import platform.Foundation.NSURLCreationDateKey
import platform.Foundation.NSURLResourceKey
import platform.Foundation.NSURLTypeIdentifierKey
import platform.Foundation.timeIntervalSince1970
import platform.UniformTypeIdentifiers.UTType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable(with = PlatformFileSerializer::class)
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
    if (path.isAbsolute) {
        PlatformFile(NSURL.fileURLWithPath(path = path.toString()))
    } else {
        PlatformFile(NSURL(string = path.toString()))
    }

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

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? {
    val values = this.nsUrl.resourceValuesForKeys(listOf(NSURLCreationDateKey), null)
    val date = values?.get(NSURLCreationDateKey) as? NSDate
    return Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant {
    val values = this.nsUrl.resourceValuesForKeys(listOf(NSURLContentModificationDateKey), null)
    val date = values?.get(NSURLContentModificationDateKey) as? NSDate
    return Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
}

public actual fun PlatformFile.mimeType(): MimeType? = withScopedAccess { file ->
    file.nsUrl.mimeTypeFromMetadata()
        ?: file.nsUrl.mimeTypeFromExtension()
}?.takeIf { it.isNotBlank() }?.let(MimeType::parse)

private fun NSURL.mimeTypeFromMetadata(): String? {
    val contentType = resourceValue(NSURLContentTypeKey) as? UTType

    val fromContentType = contentType?.preferredMIMEType
    if (!fromContentType.isNullOrBlank()) {
        return fromContentType
    }

    val identifier = contentType?.identifier
        ?: resourceValue(NSURLTypeIdentifierKey) as? String

    return identifier?.let(::mimeTypeFromUti)
}

private fun NSURL.mimeTypeFromExtension(): String? =
    pathExtension
        ?.takeIf { it.isNotBlank() }
        ?.let { ext -> UTType.typeWithFilenameExtension(ext)?.preferredMIMEType }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun NSURL.resourceValue(key: NSURLResourceKey?): Any? = memScoped {
    val valuePtr = alloc<ObjCObjectVar<Any?>>()
    val success = getResourceValue(value = valuePtr.ptr, forKey = key, error = null)
    if (success) valuePtr.value else null
}

@OptIn(ExperimentalForeignApi::class)
private fun mimeTypeFromUti(uti: String): String? {
    if (uti.isBlank()) {
        return null
    }

    return memScoped {
        val cfUti = CFStringCreateWithCString(
            alloc = kCFAllocatorDefault,
            cStr = uti,
            encoding = kCFStringEncodingUTF8
        ) ?: return@memScoped null

        val mimeRef = UTTypeCopyPreferredTagWithClass(
            inUTI = cfUti,
            inTagClass = kUTTagClassMIMEType
        )

        CFRelease(cfUti)

        val mimeValue = cfStringToKString(mimeRef)

        mimeRef?.let { CFRelease(it) }

        mimeValue
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

public actual fun PlatformFile.releaseBookmark() {}

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
