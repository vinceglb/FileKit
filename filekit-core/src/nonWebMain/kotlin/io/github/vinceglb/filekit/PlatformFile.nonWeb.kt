package io.github.vinceglb.filekit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
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
 * Returns the path string of this file.
 */
public expect val PlatformFile.path: String

/**
 * Returns the parent of this file, or null if it does not have a parent.
 *
 * @return The parent [PlatformFile], or null.
 */
public expect fun PlatformFile.parent(): PlatformFile?

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
 * Checks if this file is a regular file.
 *
 * @return `true` if it is a regular file, `false` otherwise.
 */
public expect fun PlatformFile.isRegularFile(): Boolean

/**
 * Checks if this file is a directory.
 *
 * @return `true` if it is a directory, `false` otherwise.
 */
public expect fun PlatformFile.isDirectory(): Boolean

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

/**
 * Returns the last modification time of this file.
 *
 * @return The [Instant] of the last modification.
 */
@OptIn(ExperimentalTime::class)
public expect fun PlatformFile.lastModified(): Instant

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
        platformFile.source().use { source ->
            val size = platformFile.size()
            this@write
                .sink()
                .buffered()
                .use { it.write(source, size) }
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
    resolvedDestination write this
}

internal expect suspend fun PlatformFile.prepareDestinationForWrite(source: PlatformFile): PlatformFile

/**
 * Creates directories at this file path.
 *
 * @param mustCreate If `true`, fails if the directory already exists. Defaults to `false`.
 */
public fun PlatformFile.createDirectories(mustCreate: Boolean = false): Unit =
    SystemFileSystem.createDirectories(toKotlinxIoPath(), mustCreate)

/**
 * Lists the files in this directory and passes them to the given block.
 *
 * @param block A callback function that receives a list of [PlatformFile]s.
 */
public expect inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit)

/**
 * Lists the files in this directory.
 *
 * @return A list of [PlatformFile]s in this directory.
 */
public expect fun PlatformFile.list(): List<PlatformFile>

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
 */
public expect suspend fun PlatformFile.delete(mustExist: Boolean = true)

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
 * @param bookmarkData The [BookmarkData] to resolve.
 * @return A [PlatformFile] instance resolved from the bookmark.
 */
public expect fun PlatformFile.Companion.fromBookmarkData(bookmarkData: BookmarkData): PlatformFile

/**
 * Creates a [PlatformFile] from a byte array representing bookmark data.
 *
 * @param bytes The byte array containing bookmark data.
 * @return A [PlatformFile] instance resolved from the bookmark data.
 */
public fun PlatformFile.Companion.fromBookmarkData(bytes: ByteArray): PlatformFile =
    fromBookmarkData(BookmarkData(bytes))
