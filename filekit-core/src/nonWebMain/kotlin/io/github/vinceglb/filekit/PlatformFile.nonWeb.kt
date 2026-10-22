package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Creates a [PlatformFile] from a [Path].
 *
 * @param path The [Path] to create the file from.
 * @return A [PlatformFile] instance representing the path.
 */
public expect fun PlatformFile(path: Path): PlatformFile

/**
 * Creates a [PlatformFile] from a string path.
 *
 * @param path The string representation of the file path.
 * @return A [PlatformFile] instance representing the path.
 */
public expect fun PlatformFile(path: String): PlatformFile

/**
 * Creates a [PlatformFile] from a parent [PlatformFile] and a child path string.
 *
 * @param base The parent [PlatformFile].
 * @param child The child path string.
 * @return A [PlatformFile] instance representing the combined path.
 */
public expect fun PlatformFile(base: PlatformFile, child: String): PlatformFile

/**
 * Converts this [PlatformFile] to a [Path].
 *
 * @return The [Path] representation of this file.
 */
public expect fun PlatformFile.toKotlinxIoPath(): Path

/**
 * Returns the absolute path string of this file.
 *
 * @return The absolute path string.
 */
public expect fun PlatformFile.absolutePath(): String

/**
 * Returns the absolute file corresponding to this file.
 *
 * @return The absolute [PlatformFile].
 */
public expect fun PlatformFile.absoluteFile(): PlatformFile

/**
 * Opens a [RawSource] to read from this file.
 *
 * @return A [RawSource] for reading.
 */
public expect fun PlatformFile.source(): RawSource

/**
 * Opens a [RawSink] to write to this file.
 *
 * @param append Whether to append to the file if it exists. Defaults to false.
 * @return A [RawSink] for writing.
 */
public expect fun PlatformFile.sink(append: Boolean = false): RawSink

/**
 * Checks if this file path is absolute.
 *
 * @return `true` if the path is absolute, `false` otherwise.
 */
public expect fun PlatformFile.isAbsolute(): Boolean

/**
 * Checks if this file exists.
 *
 * @return `true` if the file exists, `false` otherwise.
 */
public expect fun PlatformFile.exists(): Boolean

/**
 * Returns the creation time of this file.
 *
 * @return The [Instant] of creation, or null if it cannot be determined.
 */
@OptIn(ExperimentalTime::class)
public expect fun PlatformFile.createdAt(): Instant?

public actual suspend fun PlatformFile.readBytes(): ByteArray =
    withContext(Dispatchers.IO) {
        this@readBytes
            .source()
            .buffered()
            .use { it.readByteArray() }
    }

public actual suspend fun PlatformFile.readString(): String =
    withContext(Dispatchers.IO) {
        this@readString
            .source()
            .buffered()
            .use { it.readString() }
    }

public actual suspend fun PlatformFile.sizeRecursively(): Long =
    withContext(Dispatchers.IO) { computeSizeRecursively() }

/**
 * Writes the given bytes to this file.
 *
 * @param bytes The bytes to write.
 */
public suspend infix fun PlatformFile.write(bytes: ByteArray): Unit =
    withContext(Dispatchers.IO) {
        this@write
            .sink()
            .buffered()
            .use { it.write(bytes) }
    }

/**
 * Writes the content of another [PlatformFile] to this file.
 *
 * @param platformFile The source [PlatformFile] to read from.
 */
public suspend infix fun PlatformFile.write(platformFile: PlatformFile): Unit =
    withContext(Dispatchers.IO) {
        if (platformFile.isDirectory()) {
            throw FileKitException("Cannot write a directory to a file destination.")
        }
        if (this@write.isSameLogicalFileAs(platformFile)) {
            throw FileKitException("Source and destination refer to the same file.")
        }

        platformFile.source().use { source ->
            this@write
                .sink()
                .use { sink ->
                    val buffer = Buffer()
                    while (true) {
                        val bytesRead = source.readAtMostTo(buffer, COPY_BUFFER_SIZE_BYTES)
                        if (bytesRead == -1L) {
                            break
                        }
                        sink.write(buffer, bytesRead)
                    }
                    sink.flush()
                }
        }
    }

/**
 * Writes the given string to this file.
 *
 * @param string The string to write.
 */
public suspend fun PlatformFile.writeString(string: String): Unit =
    withContext(Dispatchers.IO) {
        this@writeString
            .sink()
            .buffered()
            .use { it.writeString(string) }
    }

/**
 * Copies this file to the destination [PlatformFile].
 *
 * @param destination The destination [PlatformFile].
 */
public suspend infix fun PlatformFile.copyTo(destination: PlatformFile) {
    val resolvedDestination = destination.prepareDestinationForWrite(source = this)
    if (resolvedDestination.isSameLogicalFileAs(this)) {
        throw FileKitException("Source and destination refer to the same file.")
    }
    resolvedDestination write this
}

/**
 * Copies this file or directory tree to [target], which becomes the new root (`source/a.txt` is
 * copied to `target/a.txt`). Missing parent directories are created and existing directories are
 * merged. Runs on `Dispatchers.IO`.
 *
 * Fails on the first error, leaving [target] partially copied. Symbolic links (and Windows reparse
 * points) inside the tree are skipped, never followed; copying a link itself throws, as does copying
 * a tree into itself. On Android, new subdirectories can only be created in a `content://` [target]
 * whose provider uses path-based document IDs, such as device storage; other providers throw.
 *
 * @param target The root of the copy.
 * @param overwrite If `true`, replaces existing files. If `false`, throws on the first existing
 * file. A file never replaces a directory, or the other way around.
 */
public suspend fun PlatformFile.copyRecursively(
    target: PlatformFile,
    overwrite: Boolean = false,
): Unit = withContext(Dispatchers.IO) {
    val source = this@copyRecursively
    source.withScopedAccess {
        target.withScopedAccess {
            if (source.isSymbolicLink()) throw FileKitException("Cannot copy a symbolic link: $source")
            if (!source.exists()) throw FileKitException("Source does not exist: $source")
            if (target.isSameOrInside(source)) throw FileKitException("Cannot copy $source into itself: $target")

            if (!source.isDirectory()) target.parent()?.createDirectories()
            // Snapshot the tree before writing: even through an alias that can't be resolved (such as a
            // Windows junction), the copy never reaches its own output.
            val entries = mutableListOf<CopyEntry>()
            source.collectCopyEntries(target, entries)
            entries.forEach { it.copy(root = source, overwrite) }
        }
    }
}

private class CopyEntry(
    val source: PlatformFile,
    val target: PlatformFile,
    val isDirectory: Boolean,
)

private fun PlatformFile.collectCopyEntries(target: PlatformFile, entries: MutableList<CopyEntry>) {
    val isDirectory = isDirectory()
    entries += CopyEntry(this, target, isDirectory)
    if (!isDirectory) return
    val names = mutableSetOf<String>()
    list().forEach { child ->
        // Check for a link first: isDirectory() resolves the link and touches its target.
        if (child.isSymbolicLink()) return@forEach
        // Android document providers choose their own names: never let one escape the target, and
        // never let two siblings with the same name overwrite each other.
        val name = child.name
        if (name.isEmpty() || name == "." || name == ".." || '/' in name || !names.add(name)) {
            throw FileKitException("Cannot copy an entry named \"$name\" from $this")
        }
        child.collectCopyEntries(target / name, entries)
    }
}

// Each existing target is resolved against the whole source tree: a link in the target can point
// into any part of it, not only into the entry being copied.
private suspend fun CopyEntry.copy(root: PlatformFile, overwrite: Boolean) {
    if (isDirectory) {
        if (target.isRegularFile()) throw FileKitException("Cannot replace file $target with directory $source")
        target.createDirectories()
        // Now that the target exists, resolve links and ".." that could make it alias the source.
        if (target.isResolvedInside(root)) throw FileKitException("Cannot copy $root into itself: $target")
    } else {
        if (target.isDirectory()) throw FileKitException("Cannot replace directory $target with file $source")
        if (target.exists()) {
            if (!overwrite) throw FileKitException("Destination already exists: $target")
            if (target.isResolvedInside(root)) throw FileKitException("Cannot copy $root into itself: $target")
        }
        target write source
    }
}

private fun PlatformFile.isSameOrInside(directory: PlatformFile): Boolean =
    comparablePath().isSameOrInside(directory.comparablePath())

private fun PlatformFile.isResolvedInside(directory: PlatformFile): Boolean =
    absoluteFile().isSameOrInside(directory.absoluteFile())

// An Android document can be reached through several tree URIs: identify it by authority and ID.
private fun PlatformFile.comparablePath(): String {
    val path = absolutePath()
    if (!path.startsWith("content://")) return path
    val authority = path.removePrefix("content://").substringBefore('/')
    return "content://$authority/" + path.substringAfterLast("/document/", path.substringAfterLast("/tree/"))
}

private fun String.isSameOrInside(directory: String): Boolean {
    // Only Windows paths use backslashes, and only Android document IDs encode "/" as "%2F".
    val isWindows = directory.getOrNull(1) == ':' || directory.startsWith("\\\\")
    val separators = when {
        directory.startsWith("content://") -> listOf("/", "%2F")
        isWindows -> listOf("/", "\\")
        else -> listOf("/")
    }
    val trailing = if (isWindows) charArrayOf('/', '\\') else charArrayOf('/')
    val path = trimEnd(*trailing)
    val root = directory.trimEnd(*trailing)
    return path == root || separators.any { path.startsWith(root + it) }
}

private fun PlatformFile.isSameLogicalFileAs(other: PlatformFile): Boolean {
    val thisAbsolute = absolutePath()
    val otherAbsolute = other.absolutePath()
    return if (thisAbsolute.isNotBlank() && otherAbsolute.isNotBlank()) {
        thisAbsolute == otherAbsolute
    } else {
        path == other.path
    }
}

private const val COPY_BUFFER_SIZE_BYTES: Long = 8_192L

internal expect suspend fun PlatformFile.prepareDestinationForWrite(source: PlatformFile): PlatformFile

/**
 * Creates directories at this file path.
 *
 * @param mustCreate If `true`, fails if the directory already exists. Defaults to `false`.
 */
public expect fun PlatformFile.createDirectories(mustCreate: Boolean = false)

/**
 * Atomically moves this file to the destination.
 *
 * @param destination The destination [PlatformFile].
 */
public expect suspend fun PlatformFile.atomicMove(destination: PlatformFile)

/**
 * Deletes this file.
 *
 * @param mustExist If `true`, fails if the file does not exist. Defaults to `true`.
 * @param recursively If `true`, a directory is emptied before it is removed. Defaults to `false`,
 * which fails on a filesystem directory that still has contents. Symbolic links (including dangling
 * links) and Windows directory junctions are unlinked, never followed. Android document URI deletion
 * is handled by the document provider regardless of this flag.
 */
public expect suspend fun PlatformFile.delete(
    mustExist: Boolean = true,
    recursively: Boolean = false,
)

/**
 * Empties this directory, depth first, leaving the directory itself in place. Does nothing when
 * this is not a directory.
 *
 * The caller must handle links with [deleteIfSymbolicLink] before entering this function.
 */
internal suspend fun PlatformFile.deleteChildren() {
    if (!isDirectory()) return
    list().forEach { child ->
        child.delete(mustExist = false, recursively = true)
    }
}

/**
 * Unlinks this entry and returns `true` if it is a symbolic link or a Windows reparse point.
 * Returns `false` for other entries and missing paths. Inspection and deletion must not follow the
 * target: it can be missing, cyclic, or outside the tree being deleted. Deletion failures propagate.
 */
internal expect fun PlatformFile.deleteIfSymbolicLink(): Boolean

/**
 * Appends a child path to this [PlatformFile].
 *
 * @param child The child path string.
 * @return A [PlatformFile] instance representing the combined path.
 */
public operator fun PlatformFile.div(child: String): PlatformFile =
    PlatformFile(this, child)

/**
 * Resolves a relative path against this [PlatformFile].
 *
 * @param relative The relative path string.
 * @return A [PlatformFile] instance representing the resolved path.
 */
public fun PlatformFile.resolve(relative: String): PlatformFile =
    this / relative

/**
 * Creates bookmark data for this file.
 *
 * This is primarily used on Apple platforms to persist access to security-scoped resources.
 *
 * @return A [BookmarkData] object containing the bookmark.
 */
public expect suspend fun PlatformFile.bookmarkData(): BookmarkData

/**
 * Releases the bookmark data associated with this file.
 */
public expect fun PlatformFile.releaseBookmark()

/**
 * Creates a [PlatformFile] from [BookmarkData].
 *
 * This remains the convenience API for callers that only need the restored file. Use
 * [resolveBookmarkData] when the application also needs staleness and refresh metadata.
 *
 * @param bookmarkData The [BookmarkData] to resolve.
 * @return A [PlatformFile] instance resolved from the bookmark.
 */
public expect fun PlatformFile.Companion.fromBookmarkData(bookmarkData: BookmarkData): PlatformFile

/**
 * Resolves [bookmarkData] and reports whether the persisted representation should be refreshed.
 */
public expect fun PlatformFile.Companion.resolveBookmarkData(bookmarkData: BookmarkData): BookmarkResolution

/**
 * Creates a [PlatformFile] from a byte array representing bookmark data.
 *
 * @param bytes The byte array containing bookmark data.
 * @return A [PlatformFile] instance resolved from the bookmark data.
 */
public fun PlatformFile.Companion.fromBookmarkData(bytes: ByteArray): PlatformFile =
    fromBookmarkData(BookmarkData(bytes))

/** Resolves bookmark data stored as a byte array. */
public fun PlatformFile.Companion.resolveBookmarkData(bytes: ByteArray): BookmarkResolution =
    resolveBookmarkData(BookmarkData(bytes))
