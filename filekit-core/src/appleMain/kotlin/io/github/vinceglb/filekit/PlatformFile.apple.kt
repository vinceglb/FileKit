package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toKotlinxPath
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
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
import platform.Foundation.NSLock
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

/**
 * Represents a file on the Apple platform (iOS/macOS).
 *
 * @property nsUrl The underlying [NSURL] object.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual class PlatformFile private constructor(
    public val nsUrl: NSURL,
    internal val macOsBookmarkLease: MacOsBookmarkLease?,
) {
    public constructor(nsUrl: NSURL) : this(nsUrl, null)

    public actual override fun toString(): String = path

    public operator fun component1(): NSURL = nsUrl

    public fun copy(nsUrl: NSURL = this.nsUrl): PlatformFile = PlatformFile(
        nsUrl = nsUrl,
        macOsBookmarkLease = macOsBookmarkLease?.takeIf { it.covers(nsUrl) },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformFile) return false
        return nsUrl.path == other.nsUrl.path
    }

    override fun hashCode(): Int = nsUrl.path.hashCode()

    public actual companion object {
        internal fun withMacOsBookmarkLease(url: NSURL): PlatformFile =
            PlatformFile(url, MacOsBookmarkLease(url))
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class MacOsBookmarkLease(
    url: NSURL,
) {
    private val lock = NSLock()
    private val rootPath = url.standardizedPath
    private var scopeUrl: NSURL? = url
    private var activeAccesses = 0
    private var released = false

    fun covers(url: NSURL): Boolean = url.standardizedPath.isWithin(rootPath)

    fun start(): Boolean = lock.withLock {
        check(!released) { "This security-scoped bookmark has been released" }
        val granted = requireNotNull(scopeUrl).startAccessingSecurityScopedResource()
        if (granted) {
            activeAccesses += 1
        }
        return granted
    }

    fun stop(): Unit = lock.withLock {
        if (activeAccesses == 0) return@withLock
        requireNotNull(scopeUrl).stopAccessingSecurityScopedResource()
        activeAccesses -= 1
        releaseNativeUrlIfDrained()
    }

    fun release(): Unit = lock.withLock {
        if (released) return@withLock
        released = true
        releaseNativeUrlIfDrained()
    }

    private fun releaseNativeUrlIfDrained() {
        if (released && activeAccesses == 0) {
            scopeUrl = null
        }
    }
}

private inline fun <T> NSLock.withLock(block: () -> T): T {
    lock()
    return try {
        block()
    } finally {
        unlock()
    }
}

public actual fun PlatformFile(path: Path): PlatformFile =
    if (path.isAbsolute) {
        PlatformFile(NSURL.fileURLWithPath(path = path.toString()))
    } else {
        PlatformFile(NSURL(string = path.toString()))
    }

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    nsUrl.toKotlinxPath()

@PublishedApi
internal actual fun PlatformFile.withPath(path: Path): PlatformFile =
    copy(PlatformFile(path).nsUrl)

private fun String.isWithin(rootPath: String): Boolean {
    val root = rootPath.trimEnd('/')
    return when {
        root.isEmpty() && rootPath.startsWith('/') -> startsWith('/')
        this == root -> true
        root.isNotEmpty() -> startsWith("$root/")
        else -> false
    }
}

private val NSURL.standardizedPath: String
    get() {
        val unresolvedSegments = mutableListOf<String>()
        var candidate: Path? = toKotlinxPath()
        while (candidate != null) {
            val resolved = runCatching { SystemFileSystem.resolve(candidate) }.getOrNull()
            if (resolved != null) {
                return unresolvedSegments
                    .asReversed()
                    .fold(resolved) { path, segment -> Path(path, segment) }
                    .toString()
            }
            unresolvedSegments += candidate.name
            candidate = candidate.parent
        }
        return URLByStandardizingPath?.path.orEmpty()
    }

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
            .map(::withPath)
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem
            .list(toKotlinxIoPath())
            .map(::withPath)
    }

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = withScopedAccess {
    val values = nsUrl.resourceValuesForKeys(listOf(NSURLCreationDateKey), null)
    val date = values?.get(NSURLCreationDateKey) as? NSDate
    Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant = withScopedAccess {
    val values = nsUrl.resourceValuesForKeys(listOf(NSURLContentModificationDateKey), null)
    val date = values?.get(NSURLContentModificationDateKey) as? NSDate
    Instant.fromEpochSeconds(date?.timeIntervalSince1970?.toLong() ?: 0L)
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
private fun NSURL.resourceValue(key: NSURLResourceKey): Any? = memScoped {
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
            encoding = kCFStringEncodingUTF8,
        ) ?: return@memScoped null

        val mimeRef = UTTypeCopyPreferredTagWithClass(
            inUTI = cfUti,
            inTagClass = kUTTagClassMIMEType,
        )

        CFRelease(cfUti)

        val mimeValue = cfStringToKString(mimeRef)

        mimeRef?.let { CFRelease(it) }

        mimeValue
    }
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
private fun cfStringToKString(cfString: CFStringRef?): String? {
    if (cfString == null) {
        return null
    }

    val length = CFStringGetLength(cfString)

    val maxSize = CFStringGetMaximumSizeForEncoding(
        length = length,
        encoding = kCFStringEncodingUTF8,
    ) + 1

    return memScoped {
        val buffer = allocArray<ByteVar>(maxSize)

        if (
            CFStringGetCString(
                theString = cfString,
                buffer = buffer,
                bufferSize = maxSize,
                encoding = kCFStringEncodingUTF8,
            )
        ) {
            buffer.toKString()
        } else {
            null
        }
    }
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean =
    macOsBookmarkLease?.start() ?: nsUrl.startAccessingSecurityScopedResource()

public actual fun PlatformFile.stopAccessingSecurityScopedResource(): Unit =
    macOsBookmarkLease?.stop() ?: nsUrl.stopAccessingSecurityScopedResource()

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class, BetaInteropApi::class)
public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    withScopedAccess {
        memScoped {
            val configuration = appleBookmarkCreationConfiguration()
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            val bookmarkData = nsUrl.bookmarkDataWithOptions(
                options = configuration.options.convert(),
                includingResourceValuesForKeys = null,
                relativeToURL = null,
                error = errorPtr.ptr,
            ) ?: throw FileKitException("Failed to create bookmark data: ${errorPtr.ptr.pointed.value}")
            BookmarkData(
                encodeAppleBookmarkPayload(
                    payload = bookmarkData.toByteArray(),
                    configuration = configuration,
                ),
            )
        }
    }
}

public actual fun PlatformFile.releaseBookmark() {
    macOsBookmarkLease?.release()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, UnsafeNumber::class)
public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile = resolveBookmarkData(bookmarkData).file

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, UnsafeNumber::class)
public actual fun PlatformFile.Companion.resolveBookmarkData(
    bookmarkData: BookmarkData,
): BookmarkResolution {
    val payload = decodeAppleBookmarkPayload(bookmarkData.bytes)
    val nativeResolution = resolveAppleBookmark(
        payload.bytes,
        payload.resolutionOptions,
    )
    return appleBookmarkResolution(payload, nativeResolution)
}

internal fun appleBookmarkResolution(
    payload: AppleBookmarkPayload,
    nativeResolution: AppleBookmarkNativeResolution,
): BookmarkResolution {
    val file = if (payload.kind == MacOsBookmarkKind.SecurityScoped) {
        PlatformFile.withMacOsBookmarkLease(nativeResolution.url)
    } else {
        PlatformFile(nativeResolution.url)
    }
    return BookmarkResolution(
        file = file,
        isStale = nativeResolution.isStale,
        shouldRefresh = nativeResolution.isStale || payload.isLegacy,
    )
}

internal data class AppleBookmarkNativeResolution(
    val url: NSURL,
    val isStale: Boolean,
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class, UnsafeNumber::class)
private fun resolveAppleBookmark(
    bytes: ByteArray,
    resolutionOptions: ULong,
): AppleBookmarkNativeResolution = memScoped {
    val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
    val isStale = alloc<BooleanVar>()
    val restoredUrl = NSURL.URLByResolvingBookmarkData(
        bookmarkData = bytes.toNSData(),
        options = resolutionOptions.convert(),
        relativeToURL = null,
        bookmarkDataIsStale = isStale.ptr,
        error = error,
    ) ?: throw error.pointed.value.toBookmarkResolutionException()

    AppleBookmarkNativeResolution(
        url = restoredUrl,
        isStale = isStale.value,
    )
}

private fun NSError?.toBookmarkResolutionException(): BookmarkResolutionException = BookmarkResolutionException(
    reason = classifyAppleBookmarkResolutionError(this),
    message = "Failed to resolve bookmark data: $this",
)
