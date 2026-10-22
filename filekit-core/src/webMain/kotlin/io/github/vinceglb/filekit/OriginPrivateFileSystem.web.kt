@file:OptIn(ExperimentalFileKitApi::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

/**
 * The root directory of the browser's origin private file system (OPFS).
 *
 * OPFS gives each origin a private, persistent storage area that FileKit can read, write, and
 * delete. Browsers may evict it under storage pressure unless the app requests persistent storage.
 * It is available only in secure contexts in browsers that implement the File System API; the
 * browser is first contacted when the directory is used.
 *
 * Navigate it with [resolveFile], [resolveDirectory], and [listEntries].
 */
@ExperimentalFileKitApi
public val FileKit.originPrivateDir: PlatformFile
    get() = originPrivateRoot

private val originPrivateRoot: PlatformFile = PlatformFile(
    WebFile.OriginPrivateDirectory(
        resolvedHandle = null,
        path = "",
        parent = null,
    ),
)

/**
 * Returns the file at [relative] inside this origin private file system directory.
 *
 * @param relative A path relative to this directory, such as `"notes/today.txt"`.
 * Empty segments, from a leading, trailing, or repeated `/`, are ignored, as with `resolve` on
 * other platforms.
 * @param create If `true`, creates the file and its missing parent directories. Otherwise a missing
 * entry fails.
 */
@ExperimentalFileKitApi
public suspend fun PlatformFile.resolveFile(
    relative: String,
    create: Boolean = false,
): PlatformFile {
    val (parent, name) = resolveParent(relative, create)
    return PlatformFile(parent.file(name, create))
}

/**
 * Returns the directory at [relative] inside this origin private file system directory.
 *
 * @param relative A path relative to this directory, such as `"notes/2026"`.
 * Empty segments, from a leading, trailing, or repeated `/`, are ignored, as with `resolve` on
 * other platforms.
 * @param create If `true`, creates the directory and its missing parent directories. Otherwise a
 * missing entry fails.
 */
@ExperimentalFileKitApi
public suspend fun PlatformFile.resolveDirectory(
    relative: String,
    create: Boolean = false,
): PlatformFile {
    val (parent, name) = resolveParent(relative, create)
    return PlatformFile(parent.directory(name, create))
}

/**
 * Lists the current entries of this directory.
 *
 * Works for origin private file system directories, which can only be read asynchronously, and for
 * directories returned by browser pickers.
 */
@ExperimentalFileKitApi
public suspend fun PlatformFile.listEntries(): List<PlatformFile> = when (val directory = webFile) {
    is WebFile.OriginPrivateDirectory -> directory.list()
    is WebFile.DirectoryWrapper -> directory.children.map { it.toPlatformFile() }
    else -> throw FileKitException("Cannot list a regular file")
}

/** Returns the directory that contains [relative], and the name of its last segment. */
private suspend fun PlatformFile.resolveParent(
    relative: String,
    create: Boolean,
): Pair<WebFile.OriginPrivateDirectory, String> {
    val directory = webFile as? WebFile.OriginPrivateDirectory
        ?: throw FileKitException("Only origin private file system directories can resolve entries")
    val segments = relative.split('/').filter { it.isNotEmpty() }
    require(segments.isNotEmpty()) { "The relative path must name an entry: \"$relative\"" }
    require(segments.none { it == "." || it == ".." }) {
        "The relative path cannot contain \".\" or \"..\": \"$relative\""
    }
    val parent = segments.dropLast(1).fold(directory) { current, segment ->
        current.directory(segment, create)
    }
    return parent to segments.last()
}

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun WebFile.OriginPrivateDirectory.list(): List<PlatformFile> {
    val iterator = handle().values()
    val entries = mutableListOf<PlatformFile>()

    while (true) {
        val entry = iterator.next().await()
        if (entry.done) break

        val handle = entry.value
            ?: throw FileKitException("Could not read origin private file system entry")
        val path = path.appendOriginPrivateFileSystemPath(handle.name)
        entries += PlatformFile(
            when (handle.kind) {
                "file" -> {
                    val fileHandle = handle.unsafeCast<FileSystemFileHandle>()
                    WebFile.OriginPrivateFile(
                        handle = fileHandle,
                        fileSnapshot = fileHandle.getFile().await(),
                        path = path,
                        parent = this,
                    )
                }

                "directory" -> {
                    WebFile.OriginPrivateDirectory(
                        resolvedHandle = handle.unsafeCast<FileSystemDirectoryHandle>(),
                        path = path,
                        parent = this,
                    )
                }

                else -> {
                    throw FileKitException("Unsupported origin private file system entry type: ${handle.kind}")
                }
            },
        )
    }

    return entries
}

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun WebFile.OriginPrivateDirectory.file(
    name: String,
    create: Boolean,
): WebFile.OriginPrivateFile {
    val fileHandle = handle().getFileHandle(name, FileSystemGetHandleOptions(create)).await()
    return WebFile.OriginPrivateFile(
        handle = fileHandle,
        fileSnapshot = fileHandle.getFile().await(),
        path = path.appendOriginPrivateFileSystemPath(name),
        parent = this,
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun WebFile.OriginPrivateDirectory.directory(
    name: String,
    create: Boolean,
): WebFile.OriginPrivateDirectory = WebFile.OriginPrivateDirectory(
    resolvedHandle = handle().getDirectoryHandle(name, FileSystemGetHandleOptions(create)).await(),
    path = path.appendOriginPrivateFileSystemPath(name),
    parent = this,
)

/**
 * Replaces the contents of this file with [bytes], then refreshes its metadata snapshot so that
 * `size()` and `lastModified()` reflect the write.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun WebFile.OriginPrivateFile.write(bytes: ByteArray) {
    val writable = handle.createWritable().await()
    try {
        writable.write(bytes.toWebBytes()).await()
    } finally {
        writable.close().await()
    }
    updateSnapshot(handle.getFile().await())
}

@OptIn(ExperimentalWasmJsInterop::class)
internal suspend fun WebFile.OriginPrivateDirectory.removeEntry(
    name: String,
    recursively: Boolean,
    mustExist: Boolean,
) {
    removeEntry(handle(), name, recursively, mustExist).await()
}

/** Removes an entry; a missing entry fails only when [mustExist] is `true`. */
@OptIn(ExperimentalWasmJsInterop::class)
private fun removeEntry(
    directory: FileSystemDirectoryHandle,
    name: String,
    recursive: Boolean,
    mustExist: Boolean,
): Promise<JsAny?> = js(
    "directory.removeEntry(name, { recursive: recursive }).catch(function (error) { if (!mustExist && error.name === 'NotFoundError') return null; throw error; })",
)

internal fun String.appendOriginPrivateFileSystemPath(child: String): String =
    if (isEmpty()) child else "$this/$child"

@OptIn(ExperimentalWasmJsInterop::class)
@JsName("navigator")
internal external val browserNavigator: BrowserNavigator

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface BrowserNavigator : JsAny {
    val storage: BrowserStorageManager
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface BrowserStorageManager : JsAny {
    fun getDirectory(): Promise<FileSystemDirectoryHandle>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemHandle : JsAny {
    val kind: String
    val name: String
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemFileHandle : FileSystemHandle {
    fun getFile(): Promise<BrowserFile>

    fun createWritable(): Promise<FileSystemWritableFileStream>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemDirectoryHandle : FileSystemHandle {
    fun values(): FileSystemDirectoryHandleIterator

    fun getFileHandle(name: String, options: FileSystemGetHandleOptions): Promise<FileSystemFileHandle>

    fun getDirectoryHandle(name: String, options: FileSystemGetHandleOptions): Promise<FileSystemDirectoryHandle>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemDirectoryHandleIterator : JsAny {
    fun next(): Promise<FileSystemDirectoryHandleIteratorResult>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemDirectoryHandleIteratorResult : JsAny {
    val done: Boolean
    val value: FileSystemHandle?
}

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemGetHandleOptions : JsAny {
    var create: Boolean
}

@OptIn(ExperimentalWasmJsInterop::class)
internal fun FileSystemGetHandleOptions(create: Boolean): FileSystemGetHandleOptions =
    js("({ create })")

@OptIn(ExperimentalWasmJsInterop::class)
internal external interface FileSystemWritableFileStream : JsAny {
    fun write(data: JsAny): Promise<JsAny?>

    fun close(): Promise<JsAny?>
}

@OptIn(ExperimentalWasmJsInterop::class)
internal expect fun ByteArray.toWebBytes(): JsAny
