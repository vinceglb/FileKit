package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise

/**
 * Returns the root directory of the browser's origin private file system (OPFS).
 *
 * OPFS gives each origin a private, persistent storage area that FileKit can read, write, and
 * delete. Browsers may evict it under storage pressure unless the app requests persistent storage.
 * It is available only in secure contexts in browsers that implement the File System API.
 *
 * Navigate it with [file], [directory], and [listEntries].
 */
@OptIn(ExperimentalWasmJsInterop::class)
public suspend fun FileKit.originPrivateDirectory(): PlatformFile {
    val root = browserNavigator.storage.getDirectory().await()
    return PlatformFile(
        WebFile.OriginPrivateDirectory(
            handle = root,
            path = "",
            parent = null,
        ),
    )
}

/**
 * Returns the file named [name] in this origin private file system directory.
 *
 * @param create If `true`, creates the file when it does not exist. Otherwise a missing file fails.
 */
public suspend fun PlatformFile.file(
    name: String,
    create: Boolean = false,
): PlatformFile = when (val directory = webFile) {
    is WebFile.OriginPrivateDirectory -> directory.file(name, create)
    else -> throw FileKitException("Only origin private file system directories can open children")
}

/**
 * Returns the directory named [name] in this origin private file system directory.
 *
 * @param create If `true`, creates the directory when it does not exist. Otherwise a missing
 * directory fails.
 */
public suspend fun PlatformFile.directory(
    name: String,
    create: Boolean = false,
): PlatformFile = when (val directory = webFile) {
    is WebFile.OriginPrivateDirectory -> directory.directory(name, create)
    else -> throw FileKitException("Only origin private file system directories can open children")
}

/**
 * Lists the current entries of this directory.
 *
 * Works for origin private file system directories, which can only be read asynchronously, and for
 * directories returned by browser pickers.
 */
public suspend fun PlatformFile.listEntries(): List<PlatformFile> = when (val directory = webFile) {
    is WebFile.OriginPrivateDirectory -> directory.list()
    is WebFile.DirectoryWrapper -> directory.children.map { it.toPlatformFile() }
    else -> throw FileKitException("Cannot list a regular file")
}

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun WebFile.OriginPrivateDirectory.list(): List<PlatformFile> {
    val iterator = handle.values()
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
                        handle = handle.unsafeCast<FileSystemDirectoryHandle>(),
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
): PlatformFile {
    val fileHandle = handle.getFileHandle(name, FileSystemGetHandleOptions(create)).await()
    return PlatformFile(
        WebFile.OriginPrivateFile(
            handle = fileHandle,
            fileSnapshot = fileHandle.getFile().await(),
            path = path.appendOriginPrivateFileSystemPath(name),
            parent = this,
        ),
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun WebFile.OriginPrivateDirectory.directory(
    name: String,
    create: Boolean,
): PlatformFile = PlatformFile(
    WebFile.OriginPrivateDirectory(
        handle = handle.getDirectoryHandle(name, FileSystemGetHandleOptions(create)).await(),
        path = path.appendOriginPrivateFileSystemPath(name),
        parent = this,
    ),
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
    removeEntry(handle, name, recursively, mustExist).await()
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
private external val browserNavigator: BrowserNavigator

@OptIn(ExperimentalWasmJsInterop::class)
private external interface BrowserNavigator : JsAny {
    val storage: BrowserStorageManager
}

@OptIn(ExperimentalWasmJsInterop::class)
private external interface BrowserStorageManager : JsAny {
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
