@file:OptIn(ExperimentalFileKitApi::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.files.Blob
import org.w3c.files.FilePropertyBag
import org.w3c.files.FileReader
import kotlin.collections.forEachIndexed
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.unsafeCast
import kotlin.time.Instant

/**
 * Represents a file on the Web platform.
 *
 * Web directory pickers expose selected directories as virtual trees built from
 * the files returned by the browser.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual data class PlatformFile(
    public val webFile: WebFile,
) {
    public actual override fun toString(): String = path

    public actual companion object {

        /**
         * Creates a [PlatformFile] from a browser [Blob].
         *
         * Since a browser [File] is a subtype of [Blob], it can also be passed directly.
         */
        @OptIn(ExperimentalWasmJsInterop::class)
        public operator fun invoke(blob: Blob, name: String): PlatformFile {
            val file = BrowserFile(
                fileBits = listOf(blob.unsafeCast<JsAny>()).toJsArray(),
                fileName = name,
                options = FilePropertyBag(type = blob.type)
            )
            return PlatformFile(webFile = WebFile.FileWrapper(file = file))
        }

    }
}

public actual val PlatformFile.name: String
    get() = when (val file = webFile) {
        is WebFile.FileWrapper -> file.name
        is WebFile.DirectoryWrapper -> file.name
        is WebFile.OriginPrivateFile -> file.name
        is WebFile.OriginPrivateDirectory -> file.name
    }

public actual val PlatformFile.extension: String
    get() = when (webFile) {
        is WebFile.FileWrapper -> name.substringAfterLast(".", "")
        is WebFile.DirectoryWrapper -> ""
        is WebFile.OriginPrivateFile -> name.substringAfterLast(".", "")
        is WebFile.OriginPrivateDirectory -> ""
    }

public actual val PlatformFile.nameWithoutExtension: String
    get() = when (webFile) {
        is WebFile.FileWrapper -> name.substringBeforeLast(".", name)
        is WebFile.DirectoryWrapper -> name
        is WebFile.OriginPrivateFile -> name.substringBeforeLast(".", name)
        is WebFile.OriginPrivateDirectory -> name
    }

public actual fun PlatformFile.size(): Long = when (val file = webFile) {
    is WebFile.FileWrapper -> file.size
    is WebFile.DirectoryWrapper -> 0
    is WebFile.OriginPrivateFile -> file.size
    is WebFile.OriginPrivateDirectory -> 0
}

public actual val PlatformFile.path: String
    get() = when (val file = webFile) {
        is WebFile.FileWrapper -> file.path
        is WebFile.DirectoryWrapper -> file.path
        is WebFile.OriginPrivateFile -> file.path
        is WebFile.OriginPrivateDirectory -> file.path
    }

public actual fun PlatformFile.mimeType(): MimeType? = when (val file = webFile) {
    is WebFile.FileWrapper -> {
        file.type
            .takeIf { it.isNotBlank() }
            ?.let { MimeType.parse(it) }
    }

    is WebFile.DirectoryWrapper -> {
        null
    }

    is WebFile.OriginPrivateFile -> {
        file.type
            .takeIf { it.isNotBlank() }
            ?.let { MimeType.parse(it) }
    }

    is WebFile.OriginPrivateDirectory -> {
        null
    }
}

public actual fun PlatformFile.lastModified(): Instant = when (val file = webFile) {
    is WebFile.FileWrapper -> file.lastModified
    is WebFile.DirectoryWrapper -> file.lastModified
    is WebFile.OriginPrivateFile -> file.lastModified
    is WebFile.OriginPrivateDirectory -> WEB_DIRECTORY_LAST_MODIFIED
}

public actual fun PlatformFile.parent(): PlatformFile? = when (val file = webFile) {
    is WebFile.FileWrapper -> file.parent?.toPlatformFile()
    is WebFile.DirectoryWrapper -> file.parent?.toPlatformFile()
    is WebFile.OriginPrivateFile -> file.parent?.toPlatformFile()
    is WebFile.OriginPrivateDirectory -> file.parent?.toPlatformFile()
}

public actual fun PlatformFile.isRegularFile(): Boolean =
    webFile is WebFile.FileWrapper || webFile is WebFile.OriginPrivateFile

public actual fun PlatformFile.isDirectory(): Boolean =
    webFile is WebFile.DirectoryWrapper || webFile is WebFile.OriginPrivateDirectory

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit) {
    block(list())
}

public actual fun PlatformFile.list(): List<PlatformFile> = when (val file = webFile) {
    is WebFile.FileWrapper -> {
        throw FileKitException("Cannot list a regular file")
    }

    is WebFile.DirectoryWrapper -> {
        file.children.map { it.toPlatformFile() }
    }

    is WebFile.OriginPrivateFile -> {
        throw FileKitException("Cannot list a regular file")
    }

    is WebFile.OriginPrivateDirectory -> {
        throw FileKitException("Origin private file system directories must be listed with listEntries()")
    }
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.readBytes(): ByteArray = when (val file = webFile) {
    is WebFile.FileWrapper -> {
        file.file.readBytes()
    }

    is WebFile.DirectoryWrapper -> {
        throw FileKitException("Cannot read bytes from a directory")
    }

    is WebFile.OriginPrivateFile -> {
        file.handle
            .getFile()
            .await()
            .readBytes()
    }

    is WebFile.OriginPrivateDirectory -> {
        throw FileKitException("Cannot read bytes from a directory")
    }
}

public actual suspend fun PlatformFile.readString(): String =
    readBytes().decodeToString()

public actual suspend fun PlatformFile.sizeRecursively(): Long = computeSizeRecursively()

internal actual fun PlatformFile.isSymbolicLink(): Boolean = false

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun BrowserFile.readBytes(): ByteArray = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine { continuation ->
        val reader = FileReader()
        reader.onload = { event ->
            try {
                val arrayBuffer = event
                    .target
                    ?.unsafeCast<FileReader>()
                    ?.result
                    ?.unsafeCast<ArrayBuffer>()
                    ?: throw FileKitException("Could not read file")

                val bytes = Uint8Array(arrayBuffer)
                val byteArray = ByteArray(bytes.length)
                for (i in 0 until bytes.length) {
                    byteArray[i] = bytes[i]
                }

                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        reader.readAsArrayBuffer(this@readBytes)
    }
}

/**
 * Replaces the contents of this origin private file system (OPFS) file with [bytes].
 *
 * Only files obtained from [FileKit.originPrivateDir] can be written. Files returned by
 * browser pickers are read-only snapshots.
 *
 * @param bytes The bytes to write.
 */
@ExperimentalFileKitApi
public suspend infix fun PlatformFile.write(bytes: ByteArray) {
    when (val file = webFile) {
        is WebFile.OriginPrivateFile -> file.write(bytes)
        else -> throw FileKitException("Only origin private file system files can be written")
    }
}

/**
 * Replaces the contents of this origin private file system (OPFS) file with [string].
 *
 * @param string The string to write.
 */
@ExperimentalFileKitApi
public suspend fun PlatformFile.writeString(string: String) {
    write(string.encodeToByteArray())
}

/**
 * Deletes this origin private file system (OPFS) entry.
 *
 * @param mustExist If `true`, fails if the entry does not exist. Defaults to `true`.
 * @param recursively If `true`, a directory is removed with its contents. Defaults to `false`,
 * which fails on a directory that still has contents.
 */
@ExperimentalFileKitApi
public suspend fun PlatformFile.delete(
    mustExist: Boolean = true,
    recursively: Boolean = false,
) {
    val (parent, name) = when (val file = webFile) {
        is WebFile.OriginPrivateFile -> file.parent to file.name
        is WebFile.OriginPrivateDirectory -> file.parent to file.name
        else -> throw FileKitException("Only origin private file system entries can be deleted")
    }
    if (parent == null) {
        throw FileKitException("Cannot delete the origin private file system root")
    }
    parent.removeEntry(name = name, recursively = recursively, mustExist = mustExist)
}
