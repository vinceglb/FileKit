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
