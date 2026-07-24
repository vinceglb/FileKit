package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import platform.posix.stat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Wrapper for a file path on Linux platform.
 */
public class LinuxPath(
    public val path: Path,
)

/**
 * Represents a file on the Linux platform.
 *
 * @property linuxPath The underlying wrapped [Path] object.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual class PlatformFile(
    public val linuxPath: LinuxPath,
) {
    public actual override fun toString(): String = linuxPath.path.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformFile) return false
        return linuxPath.path.toString() == other.linuxPath.path.toString()
    }

    override fun hashCode(): Int = linuxPath.path.toString().hashCode()

    public actual companion object
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(linuxPath = LinuxPath(path))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    linuxPath.path

public actual val PlatformFile.extension: String
    get() = name.substringAfterLast('.', "")

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast('.', name)

public actual fun PlatformFile.absolutePath(): String {
    val rawPath = linuxPath.path.toString()
    if (rawPath.startsWith("/")) return rawPath
    return absoluteFile().linuxPath.path.toString()
}

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(it) }
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(it) }
    }

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = memScoped {
    val statBuf = alloc<stat>()
    if (platform.posix.stat(absolutePath(), statBuf.ptr) == 0) {
        Instant.fromEpochSeconds(statBuf.st_ctim.tv_sec.toLong(), statBuf.st_ctim.tv_nsec.toLong())
    } else {
        null
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant = memScoped {
    val statBuf = alloc<stat>()
    if (platform.posix.stat(absolutePath(), statBuf.ptr) == 0) {
        Instant.fromEpochSeconds(statBuf.st_mtim.tv_sec.toLong(), statBuf.st_mtim.tv_nsec.toLong())
    } else {
        Instant.fromEpochMilliseconds(0L)
    }
}

public actual fun PlatformFile.mimeType(): MimeType? = null

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData =
    withContext(Dispatchers.IO) {
        BookmarkData(absolutePath().encodeToByteArray())
    }

public actual fun PlatformFile.releaseBookmark() {}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile {
    val restoredPath = bookmarkData.bytes.decodeToString()
    return PlatformFile(Path(restoredPath))
}
