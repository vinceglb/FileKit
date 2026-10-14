package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemPathSeparator
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

/**
 * Copies this file or directory tree to [target], which becomes the new root.
 *
 * Unlike [copyTo], [target] is the final destination: a directory's contents are copied
 * directly into it without adding the source directory's name. For a regular file,
 * [target] specifies the destination file, including its name. The source is preserved.
 * For filesystem paths, missing parent directories are created, including an empty target
 * directory when the source is empty.
 *
 * Existing destination directories are merged. With [overwrite] set to `false`, existing
 * destination files are left unchanged and their source entries are skipped. With `true`,
 * conflicting files are replaced. A file/directory type conflict throws instead of copying
 * a file into a conflicting directory or replacing a directory with a file.
 *
 * Symbolic-link source entries are skipped without following their targets. If this file
 * or [target] itself is a symbolic link, the whole operation is skipped. On Windows,
 * this also applies to junctions and other reparse points. Not available on Web targets.
 *
 * Callbacks are invoked synchronously for each event. Addition and overwrite callbacks
 * receive the destination file after a successful copy; skip callbacks receive the source
 * entry. Creating or merging a directory does not invoke an addition or overwrite callback.
 * Callback exceptions propagate. The first error stops the operation without rollback,
 * so directories and files already copied, or a partially written file, may remain.
 *
 * On Apple platforms, security-scoped access to the source and target remains active
 * throughout the copy. Android document URI destinations depend on the provider's child
 * creation and parent resolution support; providers with opaque IDs are not fully supported.
 *
 * @param target The final destination file or directory. Must be different from the source
 * and outside the source directory tree. If it exists, it must match the source's type.
 * @param overwrite Whether to replace existing destination files. Defaults to `false`,
 * which skips them and continues copying other entries.
 * @param onFileSkipped Called with the source entry when it is skipped because of a
 * symbolic link or an existing destination file with [overwrite] set to `false`.
 * @param onFileAdded Called with the new destination file after it is successfully copied.
 * @param onFileOverwritten Called with the existing destination file after it is successfully replaced.
 * @throws IllegalArgumentException If the source does not exist or is not a regular file
 * or directory, the destination is the source or a detected descendant, or a file/directory
 * type conflict is found.
 */
public suspend fun PlatformFile.copyRecursively(
    target: PlatformFile,
    overwrite: Boolean = false,
    onFileSkipped: ((PlatformFile) -> Unit)? = null,
    onFileAdded: ((PlatformFile) -> Unit)? = null,
    onFileOverwritten: ((PlatformFile) -> Unit)? = null,
) {
    if(this.isSymbolicLink() || target.isSymbolicLink()) {
        onFileSkipped?.invoke(this)
        return
    }

    return withScopedAccess {
        target.withScopedAccess {
            checkIfValidCopyCombination(
                source = this,
                target = target
            )

            if(this.isRegularFile()) {
                this.copyAsSingleFile(
                    target = target,
                    overwrite = overwrite,
                    onFileSkipped = onFileSkipped,
                    onFileAdded = onFileAdded,
                    onFileOverwritten = onFileOverwritten
                )
            } else {
                this.copyAsDirectoryToDirectory(
                    target = target,
                    overwrite = overwrite,
                    onFileSkipped = onFileSkipped,
                    onFileAdded = onFileAdded,
                    onFileOverwritten = onFileOverwritten
                )
            }
        }
    }
}

/**
 * Validates the source type and an existing target before copying.
 *
 * A missing target is allowed. Existing targets must have the same type as the source
 * and must not resolve to the same file or directory. Descendant destinations are
 * checked separately by [copyAsDirectoryToDirectory].
 *
 * @param source The existing regular file or directory to copy.
 * @param target The final destination, which may not exist yet.
 * @throws IllegalArgumentException If the source is not a regular file or directory,
 * an existing target has a different type, or both resolve to the same resource.
 */
private fun checkIfValidCopyCombination(
    source: PlatformFile,
    target: PlatformFile,
) {
    val targetNotExists = !target.exists()

    val directoryToDirectory = source.isDirectory() && (target.isDirectory() || targetNotExists)
    val fileToFile = source.isRegularFile() && (target.isRegularFile() || targetNotExists)
    require(directoryToDirectory || fileToFile) {
        "The allowed combinations are directory to directory or file to file"
    }
    require(targetNotExists || source.absoluteFile() != target.absoluteFile()) {
        "Target destination must be different from the source"
    }
}

/**
 * Copies this regular file to the validated destination and reports the result.
 *
 * Creates missing parent directories when [target]'s parent can be determined.
 * The caller is responsible for validating the source and target and maintaining
 * any required security-scoped access.
 *
 * @param target The final destination file, including its name.
 * @param overwrite Whether to replace an existing destination file.
 * @param onFileSkipped Called with this source file if an existing target is left unchanged.
 * @param onFileAdded Called with the destination file after a new copy succeeds.
 * @param onFileOverwritten Called with the destination file after replacement succeeds.
 */
private suspend fun PlatformFile.copyAsSingleFile(
    target: PlatformFile,
    overwrite: Boolean = false,
    onFileSkipped: ((PlatformFile) -> Unit)? = null,
    onFileAdded: ((PlatformFile) -> Unit)? = null,
    onFileOverwritten: ((PlatformFile) -> Unit)? = null,
) {
    val parent = target.parent()
    parent?.createDirectories()

    this.copyTo(
        target = target,
        overwrite = overwrite,
        onFileSkipped = onFileSkipped,
        onFileAdded = onFileAdded,
        onFileOverwritten = onFileOverwritten
    )
}

/**
 * Copies this directory's contents directly into [target], preserving relative paths.
 *
 * Creates the target even when the source is empty. Existing directories are merged;
 * files follow [overwrite]. Source links are skipped, and callback or copy failures
 * propagate without rolling back completed entries. The caller maintains scoped access.
 *
 * @param target The validated destination directory, which must be outside the source tree.
 * @param overwrite Whether to replace existing destination files.
 * @param onFileSkipped Called with each skipped source link or file.
 * @param onFileAdded Called with each new destination file after copying succeeds.
 * @param onFileOverwritten Called with each replaced destination file after copying succeeds.
 * @throws IllegalArgumentException If [target] is a detected descendant of this directory
 * or a source file's destination is already a directory.
 */
private suspend fun PlatformFile.copyAsDirectoryToDirectory(
    target: PlatformFile,
    overwrite: Boolean = false,
    onFileSkipped: ((PlatformFile) -> Unit)? = null,
    onFileAdded: ((PlatformFile) -> Unit)? = null,
    onFileOverwritten: ((PlatformFile) -> Unit)? = null,
) {
    require(!target.isDescendantOf(this)) {
        "The target destination cannot be a descendant of the source"
    }

    target.createDirectories()

    val sourceFiles = this.walk()
    sourceFiles.forEach { sourceFile ->
        if(sourceFile.isSymbolicLink()) {
            onFileSkipped?.invoke(sourceFile)
            return@forEach
        }

        val copyFile = sourceFile.asCopyFile(
            root = this,
            target = target
        )

        if(sourceFile.isDirectory()) {
            copyFile.createDirectories()
        } else {
            require(!copyFile.isDirectory()) {
                "Cannot copy a file over a directory"
            }

            sourceFile.copyTo(
                target = copyFile,
                overwrite = overwrite,
                onFileSkipped = onFileSkipped,
                onFileAdded = onFileAdded,
                onFileOverwritten = onFileOverwritten
            )
        }
    }
}

/**
 * Checks whether a resolvable ancestor of this entry matches [file].
 *
 * Existing ancestors are compared using [absoluteFile]; missing ancestors are skipped
 * while searching their parents. An entry is not considered its own descendant.
 * Android document URI checks are limited by the provider's available parent information.
 *
 * @param file The existing file or directory to look for in this entry's ancestors.
 * @return `true` if a matching ancestor is found, or `false` if none can be resolved.
 */
private fun PlatformFile.isDescendantOf(
    file: PlatformFile
): Boolean {
    if (this == file)
        return false

    val parent = this.parent() ?: return false
    val isParentFile = parent.exists() && (parent.absoluteFile() == file.absoluteFile())

    return isParentFile || parent.isDescendantOf(
        file = file
    )
}

/**
 * Creates a destination reference preserving this source entry's path below [root].
 *
 * Does not create the destination or check for file/directory conflicts.
 *
 * @param root The source directory containing this entry.
 * @param target The destination root under which the relative path is placed.
 * @return A reference to the corresponding destination file or directory.
 * @throws IllegalArgumentException If this source entry cannot be resolved below [root].
 */
private fun PlatformFile.asCopyFile(
    root: PlatformFile,
    target: PlatformFile,
): PlatformFile {
    val relativePath = this.resolveRelativePath(
        root = root
    )

    return PlatformFile(
        base = target,
        child = relativePath,
    )
}

/**
 * Returns the path of this source entry relative to [root], excluding the root's name.
 *
 * For Android document URIs, derives the path from document names in the provider's tree.
 * Resolving a source path does not create any destination directories or documents.
 *
 * @param root The source directory containing this entry.
 * @return The child path to use below the destination directory.
 * @throws IllegalArgumentException If this entry cannot be resolved below [root].
 */
internal expect fun PlatformFile.resolveRelativePath(
    root: PlatformFile,
): String

/**
 * Derives the child path by removing the absolute directory prefix of [root].
 *
 * The paths must use a consistent representation. This helper does not canonicalize
 * symbolic links or normalize references to `.` and `..`.
 *
 * @param root The source directory whose prefix is removed.
 * @return The filesystem path below [root], excluding the root's name.
 * @throws IllegalArgumentException If this entry's absolute path does not start with
 * the absolute directory prefix of [root].
 */
internal fun PlatformFile.resolveRelativePathForFileSystem(
    root: PlatformFile,
): String {
    val separator = SystemPathSeparator
    val rootAbsolutePath = root.absolutePath()
    val prefix = rootAbsolutePath.trimEnd(separator) + separator
    val sourcePath = absolutePath()

    require(sourcePath.startsWith(prefix)) {
        "Source path is outside the root directory."
    }

    return sourcePath.removePrefix(prefix)
}

/**
 * Copies this file to a prepared file destination using the recursive copy policy.
 *
 * Leaves an existing target unchanged when [overwrite] is `false`, reporting the source
 * through [onFileSkipped]. Otherwise, reports the destination only after copying succeeds.
 * Callback exceptions propagate. The caller must validate that [target] is a file destination.
 *
 * @param target The destination file whose existence determines the copy event.
 * @param overwrite Whether to replace an existing destination file.
 * @param onFileSkipped Called with this source file when an existing target is skipped.
 * @param onFileAdded Called with [target] after copying to a new file succeeds.
 * @param onFileOverwritten Called with [target] after replacing an existing file succeeds.
 */
private suspend fun PlatformFile.copyTo(
    target: PlatformFile,
    overwrite: Boolean,
    onFileSkipped: ((PlatformFile) -> Unit)? = null,
    onFileAdded: ((PlatformFile) -> Unit)? = null,
    onFileOverwritten: ((PlatformFile) -> Unit)? = null,
) {
    val targetExists = target.exists()
    if (!overwrite && targetExists) {
        onFileSkipped?.invoke(this)
        return
    }

    this.copyTo(
        destination = target
    )

    if(targetExists) {
        onFileOverwritten?.invoke(target)
    } else {
        onFileAdded?.invoke(target)
    }
}