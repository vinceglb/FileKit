package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

/**
 * Creates a [PlatformFile] from a given [Path].
 */
public expect fun PlatformFile(path: Path): PlatformFile

/**
 * The platform-specific file path.
 * Returns `null` if the path is not available or applicable (e.g., Uri-based files on Android).
 */
public expect val PlatformFile.path: Path?

/**
 * Returns the parent directory of this [PlatformFile].
 * Returns `null` if the parent directory is not available or applicable.
 */
public expect val PlatformFile.parent: PlatformFile?

/**
 * Returns a [RawSource] for reading from this [PlatformFile].
 * Returns `null` if a source cannot be opened.
 */
public expect fun PlatformFile.source(): RawSource?     // TODO: replace by Source?

/**
 * Returns a [RawSink] for writing to this [PlatformFile].
 * If [append] is set to `true`, the content will be appended; otherwise, it will overwrite the existing content.
 * Returns `null` if a sink cannot be opened.
 */
public expect fun PlatformFile.sink(append: Boolean = false): RawSink?      // TODO: replace by Sink?

/**
 * Creates a [PlatformFile] by appending a [child] path to the [base] file's path.
 *
 * @param base The base platform file.
 * @param child The child path to append.
 * @return A new [PlatformFile] representing the combined path, or `null` if the base path is invalid.
 */
public fun PlatformFile(base: PlatformFile, child: String): PlatformFile? {
    val path = base.path ?: return null
    return PlatformFile(path / child)
}

/**
 * Returns `true` if this [PlatformFile] represents a regular file.
 * Returns `false` if it represents a directory or does not exist.
 */
public expect val PlatformFile.isFile: Boolean

/**
 * Returns `true` if this [PlatformFile] represents a directory.
 * Returns `false` if it represents a file or does not exist.
 */
public expect val PlatformFile.isDirectory: Boolean

/**
 * Returns `true` if this [PlatformFile] exists in the file system.
 * Returns `false` if it does not exist.
 */
public expect val PlatformFile.exists: Boolean

/**
 * Reads the content of the file as a [ByteArray].
 *
 * @return The content of the file as bytes, or `null` if the file cannot be read or does not exist.
 */
public actual suspend fun PlatformFile.readBytes(): ByteArray? =
    withContext(Dispatchers.IO) {
        source()?.buffered()?.readByteArray()
    }

/**
 * Writes the given [bytes] to this [PlatformFile].
 *
 * @param bytes The content to write to the file.
 * @return `true` if the write operation is successful, `false` otherwise.
 */
public suspend infix fun PlatformFile.write(bytes: ByteArray): Boolean =
    withContext(Dispatchers.IO) {
        sink()?.buffered()?.use { it.write(bytes) } != null
    }

/**
 * Writes the content of another [PlatformFile] to this file.
 *
 * @param platformFile The source file to copy from.
 * @return `true` if the copy operation is successful, `false` otherwise.
 */
public suspend infix fun PlatformFile.write(platformFile: PlatformFile): Boolean =
    withContext(Dispatchers.IO) {
        val source = platformFile.source()
        val size = platformFile.size
        if (source == null || size == null) return@withContext false
        sink()?.buffered()?.use { it.write(source, size) } != null      // TODO use transferFrom / transferTo?
    }

/**
 * Copies the content of this [PlatformFile] to the [destination] file.
 *
 * @param destination The file to copy to.
 * @return `true` if the copy operation is successful, `false` otherwise.
 */
public suspend infix fun PlatformFile.copyTo(destination: PlatformFile): Boolean =
    destination write this

/**
 * Deletes this [PlatformFile] from the file system.
 *
 * @param mustExist If set to `true`, throws an error if the file does not exist. Default is `true`.
 * @return `true` if the file is successfully deleted, `false` otherwise.
 */
public suspend fun PlatformFile.delete(mustExist: Boolean = true): Boolean =
    withContext(Dispatchers.IO) {
        path?.let { SystemFileSystem.delete(path = it, mustExist = mustExist) } != null
    }

public operator fun PlatformFile.div(child: String): PlatformFile? =
    PlatformFile(this, child)
