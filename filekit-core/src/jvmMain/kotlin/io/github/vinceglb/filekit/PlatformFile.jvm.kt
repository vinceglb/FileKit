package io.github.vinceglb.filekit

import com.sun.jna.Platform
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toKotlinxIoPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a file on the JVM platform.
 *
 * @property file The underlying [java.io.File] object.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual class PlatformFile private constructor(
    public val file: File,
    private val macOsBookmarkAccess: MacOsBookmarkAccess?,
) {
    public constructor(file: File) : this(file, null)

    public actual override fun toString(): String = path

    public operator fun component1(): File = file

    public fun copy(file: File = this.file): PlatformFile = PlatformFile(
        file = file,
        macOsBookmarkAccess = macOsBookmarkAccess?.takeIf { it.covers(file) },
    )

    override fun equals(other: Any?): Boolean = this === other || (other is PlatformFile && file == other.file)

    override fun hashCode(): Int = file.hashCode()

    @JvmSynthetic
    internal fun startMacOsBookmarkAccess(): Boolean = macOsBookmarkAccess?.start() ?: true

    @JvmSynthetic
    internal fun stopMacOsBookmarkAccess() {
        macOsBookmarkAccess?.stop()
    }

    public actual companion object {
        @JvmSynthetic
        internal fun withMacOsBookmarkAccess(
            file: File,
            access: MacOsBookmarkAccess,
        ): PlatformFile = PlatformFile(file, access)
    }
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(path.toFile())

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    file.toKotlinxIoPath()

@PublishedApi
internal actual fun PlatformFile.withPath(path: Path): PlatformFile = copy(path.toFile())

public actual val PlatformFile.extension: String
    get() = file.extension

public actual val PlatformFile.nameWithoutExtension: String
    get() = file.nameWithoutExtension

public actual fun PlatformFile.absolutePath(): String =
    file.absolutePath

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem.list(toKotlinxIoPath()).map(::withPath)
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem.list(toKotlinxIoPath()).map(::withPath)
    }

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = withScopedAccess {
    runCatching {
        val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        Instant.fromEpochMilliseconds(attributes.creationTime().toMillis())
    }.getOrNull()
}

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant = withScopedAccess {
    Instant.fromEpochMilliseconds(file.lastModified())
}

public actual fun PlatformFile.mimeType(): MimeType? = withScopedAccess {
    val mimeTypeValue = try {
        Files.probeContentType(file.toPath())
    } catch (_: Exception) {
        null
    }

    mimeTypeValue?.let(MimeType::parse)
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = startMacOsBookmarkAccess()

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {
    stopMacOsBookmarkAccess()
}

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    withScopedAccess {
        if (Platform.isMac()) {
            val kind = macOsBookmarkKindForCurrentProcess()
            BookmarkData(
                MacOsBookmarkEnvelope(
                    kind = kind,
                    payload = MacOsBookmarks.create(file, kind),
                ).encode(),
            )
        } else {
            BookmarkData(file.path.encodeToByteArray())
        }
    }
}

public actual fun PlatformFile.releaseBookmark() {}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile = resolveBookmarkData(bookmarkData).file

public actual fun PlatformFile.Companion.resolveBookmarkData(
    bookmarkData: BookmarkData,
): BookmarkResolution {
    val envelope = MacOsBookmarkEnvelope.decodeOrNull(bookmarkData.bytes)
    if (envelope != null) {
        if (!Platform.isMac()) {
            throw BookmarkResolutionException(
                reason = BookmarkResolutionFailure.INCOMPATIBLE_PLATFORM,
                message = "macOS bookmark data cannot be resolved on this platform",
            )
        }
        val resolved = MacOsBookmarks.resolve(envelope.payload, envelope.kind)
        return BookmarkResolution(
            file = resolved.access?.let { PlatformFile.withMacOsBookmarkAccess(resolved.file, it) }
                ?: PlatformFile(resolved.file),
            isStale = resolved.isStale,
            shouldRefresh = resolved.isStale,
        )
    }
    val path = try {
        bookmarkData.bytes.decodeToString(throwOnInvalidSequence = true)
    } catch (cause: CharacterCodingException) {
        throw BookmarkResolutionException(
            reason = BookmarkResolutionFailure.INVALID_DATA,
            message = "Legacy JVM bookmark data is not a valid UTF-8 path",
            cause = cause,
        )
    }
    return BookmarkResolution(
        file = PlatformFile(Path(path)),
        isStale = false,
        shouldRefresh = Platform.isMac(),
    )
}
