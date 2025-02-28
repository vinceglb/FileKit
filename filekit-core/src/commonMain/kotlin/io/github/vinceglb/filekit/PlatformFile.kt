package io.github.vinceglb.filekit

/**
 * Represents a file in a platform-specific way.
 * Each target will provide its own implementation of this class to interact with the file system.
 */
public expect class PlatformFile

/**
 * The name of the file, including its extension
 */
public expect val PlatformFile.name: String

/**
 * The file extension, which is the part of the name after the last period ('.').
 *
 * @return The file extension (e.g., "txt" for "document.txt"), or `null` if the file has no extension or the name is unavailable.
 */
public expect val PlatformFile.extension: String

/**
 * The name of the file, excluding its extension.
 *
 * @return The name of the file without the extension, or `null` if the file name is not available.
 * For example, "document.txt" becomes "document".
 */
public expect val PlatformFile.nameWithoutExtension: String

/**
 * The size of the file in bytes.
 * Returns `null` if the file does not exist or its size cannot be determined.
 */
public expect fun PlatformFile.size(): Long

/**
 * Reads the content of the file as a [ByteArray].
 *
 * @return The content of the file, or `null` if the file cannot be read or does not exist.
 * @throws Exception on read failure (platform-specific behavior). TODO does this throw an Exception?
 */
public expect suspend fun PlatformFile.readBytes(): ByteArray
