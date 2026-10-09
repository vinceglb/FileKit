package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a file on a specific platform.
 *
 * This class serves as a common abstraction for file handling across different platforms (Android, iOS, JVM, JS, etc.).
 * It provides methods to access file metadata and content.
 */
@Serializable(with = PlatformFileSerializer::class)
public expect class PlatformFile {
    override fun toString(): String

    public companion object
}

/**
 * The name of the file, including the extension.
 */
public expect val PlatformFile.name: String

/**
 * The extension of the file.
 */
public expect val PlatformFile.extension: String

/**
 * The name of the file without the extension.
 */
public expect val PlatformFile.nameWithoutExtension: String

/**
 * Returns the size of the file in bytes.
 */
public expect fun PlatformFile.size(): Long

/**
 * Returns the last modification time of this file.
 *
 * @return The [Instant] of the last modification.
 */
@OptIn(ExperimentalTime::class)
public expect fun PlatformFile.lastModified(): Instant

/**
 * Reads the content of the file as a byte array.
 *
 * @return The content of the file as a [ByteArray].
 */
public expect suspend fun PlatformFile.readBytes(): ByteArray

/**
 * Writes the given bytes to this file.
 *
 * @param bytes The bytes to write.
 */
public expect suspend infix fun PlatformFile.write(bytes: ByteArray): Unit

/**
 * Reads the content of the file as a string.
 *
 * @return The content of the file as a [String].
 */
public expect suspend fun PlatformFile.readString(): String

/**
 * Writes the given string to this file.
 *
 * @param string The string to write.
 */
public expect suspend fun PlatformFile.writeString(string: String): Unit

/**
 * Returns the MIME type of the file.
 *
 * @return The [MimeType] of the file, or null if it cannot be determined.
 */
public expect fun PlatformFile.mimeType(): MimeType?

/**
 * Returns the path string of this file.
 */
public expect val PlatformFile.path: String

/**
 * Returns the parent of this file, or null if it has no parent or its parent cannot be determined.
 *
 * On Android, content URI parents are supported for external-storage document trees.
 * Returns null at the selected tree root and for other content URIs.
 *
 * @return The parent [PlatformFile], or null.
 */
public expect fun PlatformFile.parent(): PlatformFile?

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
 * Starts accessing a security-scoped resource.
 *
 * This is primarily used on Apple platforms (iOS, macOS) to access files outside the app's sandbox.
 * On other platforms, this method returns `true` or is a no-op.
 *
 * @return `true` if access was granted or not required, `false` otherwise.
 */
public expect fun PlatformFile.startAccessingSecurityScopedResource(): Boolean

/**
 * Stops accessing a security-scoped resource.
 *
 * This should be called after finishing operations on a file accessed via [startAccessingSecurityScopedResource].
 * On platforms where security-scoped resources are not used, this method is a no-op.
 */
public expect fun PlatformFile.stopAccessingSecurityScopedResource()

/**
 * Executes the given [block] with security-scoped access to the file.
 *
 * This helper function calls [startAccessingSecurityScopedResource] before the block
 * and ensures [stopAccessingSecurityScopedResource] is called after the block, even if an exception occurs.
 *
 * @param block The code to execute with file access.
 * @return The result of the [block].
 */
public inline fun <T> PlatformFile.withScopedAccess(block: (PlatformFile) -> T): T {
    val accessGranted = startAccessingSecurityScopedResource()
    try {
        return block(this)
    } finally {
        if (accessGranted) {
            stopAccessingSecurityScopedResource()
        }
    }
}
