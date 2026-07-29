package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.Serializable
import platform.posix.getcwd
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

@PublishedApi
internal actual fun PlatformFile.withPath(path: Path): PlatformFile = PlatformFile(path)

public actual val PlatformFile.extension: String
    get() = name.substringAfterLast('.', "")

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast('.', name)

public actual fun PlatformFile.absolutePath(): String {
    val rawPath = linuxPath.path.toString()
    if (rawPath.startsWith("/")) return rawPath

    // Resolve against the working directory without requiring the file to exist,
    // matching the JVM and Windows native behaviour.
    val workingDirectory = currentWorkingDirectory() ?: return rawPath
    return when {
        rawPath.isEmpty() -> workingDirectory
        workingDirectory.endsWith("/") -> "$workingDirectory$rawPath"
        else -> "$workingDirectory/$rawPath"
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun currentWorkingDirectory(): String? = memScoped {
    val bufferSize = 4096
    val buffer = allocArray<ByteVar>(bufferSize)
    getcwd(buffer, bufferSize.convert())?.toKString()
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

/**
 * Always returns null on Linux.
 *
 * `stat` exposes no creation time. Its `st_ctim` field is the inode *status change* time, which is
 * updated by writes and metadata changes, so reporting it as a creation time would be wrong.
 * Birth time is only available through `statx(STATX_BTIME)`, which Kotlin/Native does not expose
 * and which several filesystems do not support, so null is the honest answer here.
 *
 * See https://man7.org/linux/man-pages/man7/inode.7.html
 */
@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = null

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant = memScoped {
    val statBuf = alloc<stat>()
    if (stat(absolutePath(), statBuf.ptr) == 0) {
        Instant.fromEpochSeconds(statBuf.st_mtim.tv_sec, statBuf.st_mtim.tv_nsec)
    } else {
        Instant.fromEpochMilliseconds(0L)
    }
}

public actual fun PlatformFile.mimeType(): MimeType? =
    systemMimeTypes.find(extension)

private const val SHARED_MIME_INFO_GLOBS = "/usr/share/mime/globs2"
private const val MIME_TYPES_DATABASE = "/etc/mime.types"

/**
 * Extension to MIME type lookup, split by whether the source entry demanded a case sensitive match.
 *
 * shared-mime-info marks entries such as `*.C` (C++ source) with the `cs` flag to distinguish them
 * from their case insensitive counterparts like `*.c` (C source), so the two cannot share a map.
 */
internal class SystemMimeTypes(
    private val caseSensitive: Map<String, MimeType>,
    private val caseInsensitive: Map<String, MimeType>,
) {
    fun find(extension: String): MimeType? {
        if (extension.isBlank()) return null
        return caseSensitive[extension] ?: caseInsensitive[extension.lowercase()]
    }

    fun isEmpty(): Boolean = caseSensitive.isEmpty() && caseInsensitive.isEmpty()
}

/**
 * MIME type mapping read once from the system MIME databases.
 *
 * The freedesktop.org shared-mime-info database is preferred and the Apache style `mime.types`
 * file is used as a fallback. Both are absent on minimal systems, in which case the mapping is
 * empty and [mimeType] returns null.
 */
private val systemMimeTypes: SystemMimeTypes by lazy {
    readSystemFileOrNull(SHARED_MIME_INFO_GLOBS)
        ?.let(::parseSharedMimeInfoGlobs)
        ?.takeIf { !it.isEmpty() }
        ?: readSystemFileOrNull(MIME_TYPES_DATABASE)
            ?.let(::parseMimeTypesDatabase)
        ?: SystemMimeTypes(caseSensitive = emptyMap(), caseInsensitive = emptyMap())
}

/**
 * Parses the freedesktop.org shared-mime-info database.
 *
 * `globs2` lines look like `weight:mime/type:glob[:flags]`, ordered by descending weight, for
 * example `50:text/plain:*.txt` and `50:text/x-c++src:*.C:cs`.
 *
 * See https://specifications.freedesktop.org/shared-mime-info/latest-single/
 */
internal fun parseSharedMimeInfoGlobs(content: String): SystemMimeTypes {
    val caseSensitive = mutableMapOf<String, MimeType>()
    val caseInsensitive = mutableMapOf<String, MimeType>()

    content
        .lineSequence()
        .filterNot { it.isBlank() || it.startsWith("#") }
        .forEach { line ->
            val parts = line.split(':')
            if (parts.size < 3) return@forEach

            val glob = parts[2].trim()

            // Only single suffix `*.ext` globs are usable here. `extension` is the segment after
            // the last dot, so a compound glob such as `*.tar.gz` can never be addressed by it and
            // would shadow the correct `*.gz` entry if it were registered under "gz".
            if (!glob.startsWith("*.") || glob.count { it == '.' } != 1) return@forEach

            val extension = glob.removePrefix("*.")
            val mimeType = parseMimeTypeOrNull(parts[1].trim()) ?: return@forEach
            val flags = parts.drop(3).map { it.trim() }

            // Entries are weight ordered, so the first one wins
            if (flags.contains("cs")) {
                caseSensitive.getOrPut(extension) { mimeType }
            } else {
                caseInsensitive.getOrPut(extension.lowercase()) { mimeType }
            }
        }

    return SystemMimeTypes(caseSensitive = caseSensitive, caseInsensitive = caseInsensitive)
}

/**
 * Parses an Apache style `mime.types` database, which has no case sensitive entries.
 *
 * Lines look like `text/plain    txt text`.
 */
internal fun parseMimeTypesDatabase(content: String): SystemMimeTypes {
    val caseInsensitive = mutableMapOf<String, MimeType>()

    content
        .lineSequence()
        .filterNot { it.isBlank() || it.startsWith("#") }
        .forEach { line ->
            val tokens = line.split(' ', '\t').filter(String::isNotBlank)
            if (tokens.size < 2) return@forEach

            val mimeType = parseMimeTypeOrNull(tokens[0]) ?: return@forEach
            tokens.drop(1).forEach { extension ->
                caseInsensitive.getOrPut(extension.lowercase()) { mimeType }
            }
        }

    return SystemMimeTypes(caseSensitive = emptyMap(), caseInsensitive = caseInsensitive)
}

private fun parseMimeTypeOrNull(value: String): MimeType? =
    runCatching { MimeType.parse(value) }.getOrNull()

private fun readSystemFileOrNull(location: String): String? {
    val path = Path(location)
    if (!SystemFileSystem.exists(path)) return null
    return runCatching {
        SystemFileSystem.source(path).buffered().use { it.readString() }
    }.getOrNull()
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData =
    withContext(Dispatchers.IO) {
        BookmarkData(absolutePath().encodeToByteArray())
    }

public actual fun PlatformFile.releaseBookmark() {}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile = resolveBookmarkData(bookmarkData).file

public actual fun PlatformFile.Companion.resolveBookmarkData(
    bookmarkData: BookmarkData,
): BookmarkResolution {
    val restoredPath = bookmarkData.bytes.decodeToString()
    return BookmarkResolution(
        file = PlatformFile(linuxPath = LinuxPath(Path(restoredPath))),
        isStale = false,
        shouldRefresh = false,
    )
}
