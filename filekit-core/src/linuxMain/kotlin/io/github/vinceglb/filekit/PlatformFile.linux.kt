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
 * Linux has no portable way to read a file's birth time: `statx(STATX_BTIME)` is not exposed by
 * Kotlin/Native and is unsupported by several filesystems. The inode change time (`st_ctim`) is
 * used as a best-effort approximation, which is what most Linux tooling reports as well.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = memScoped {
    val statBuf = alloc<stat>()
    if (stat(absolutePath(), statBuf.ptr) == 0) {
        Instant.fromEpochSeconds(statBuf.st_ctim.tv_sec, statBuf.st_ctim.tv_nsec)
    } else {
        null
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant = memScoped {
    val statBuf = alloc<stat>()
    if (stat(absolutePath(), statBuf.ptr) == 0) {
        Instant.fromEpochSeconds(statBuf.st_mtim.tv_sec, statBuf.st_mtim.tv_nsec)
    } else {
        Instant.fromEpochMilliseconds(0L)
    }
}

public actual fun PlatformFile.mimeType(): MimeType? {
    val ext = extension.lowercase()
    if (ext.isBlank()) return null
    return systemMimeTypesByExtension[ext]
}

private const val SHARED_MIME_INFO_GLOBS = "/usr/share/mime/globs2"
private const val MIME_TYPES_DATABASE = "/etc/mime.types"

/**
 * Extension to MIME type mapping read once from the system MIME databases.
 *
 * The freedesktop.org shared-mime-info database is preferred and the Apache style `mime.types`
 * file is used as a fallback. Both are absent on minimal systems, in which case the mapping is
 * empty and [mimeType] returns null.
 */
private val systemMimeTypesByExtension: Map<String, MimeType> by lazy {
    parseSharedMimeInfoGlobs()
        .takeIf { it.isNotEmpty() }
        ?: parseMimeTypesDatabase()
}

/**
 * Parses the freedesktop.org shared-mime-info database.
 *
 * `globs2` lines look like `50:text/plain:*.txt`, ordered by descending weight.
 */
private fun parseSharedMimeInfoGlobs(): Map<String, MimeType> =
    buildMap {
        readSystemFileOrNull(SHARED_MIME_INFO_GLOBS)
            ?.lineSequence()
            ?.filterNot { it.isBlank() || it.startsWith("#") }
            ?.forEach { line ->
                val parts = line.split(':')
                if (parts.size < 3) return@forEach

                val glob = parts[2].trim()
                // Only simple `*.ext` globs map to a single extension
                if (!glob.startsWith("*.") || glob.count { it == '.' } != 1) return@forEach

                val extension = glob.removePrefix("*.").lowercase()
                val mimeType = parseMimeTypeOrNull(parts[1].trim()) ?: return@forEach

                // Entries are weight ordered, so the first match wins
                getOrPut(extension) { mimeType }
            }
    }

/**
 * Parses an Apache style `mime.types` database.
 *
 * Lines look like `text/plain    txt text`.
 */
private fun parseMimeTypesDatabase(): Map<String, MimeType> =
    buildMap {
        readSystemFileOrNull(MIME_TYPES_DATABASE)
            ?.lineSequence()
            ?.filterNot { it.isBlank() || it.startsWith("#") }
            ?.forEach { line ->
                val tokens = line.split(' ', '\t').filter(String::isNotBlank)
                if (tokens.size < 2) return@forEach

                val mimeType = parseMimeTypeOrNull(tokens[0]) ?: return@forEach
                tokens.drop(1).forEach { extension ->
                    getOrPut(extension.lowercase()) { mimeType }
                }
            }
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
): PlatformFile {
    val restoredPath = bookmarkData.bytes.decodeToString()
    return PlatformFile(linuxPath = LinuxPath(Path(restoredPath)))
}
