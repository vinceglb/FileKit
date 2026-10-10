package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    public actual companion object
}

public actual val PlatformFile.name: String
    get() = when (val file = webFile) {
        is WebFile.FileWrapper -> file.name
        is WebFile.DirectoryWrapper -> file.name
    }

public actual val PlatformFile.extension: String
    get() = when (webFile) {
        is WebFile.FileWrapper -> name.substringAfterLast(".", "")
        is WebFile.DirectoryWrapper -> ""
    }

public actual val PlatformFile.nameWithoutExtension: String
    get() = when (webFile) {
        is WebFile.FileWrapper -> name.substringBeforeLast(".", name)
        is WebFile.DirectoryWrapper -> name
    }

public actual fun PlatformFile.size(): Long = when (val file = webFile) {
    is WebFile.FileWrapper -> file.size
    is WebFile.DirectoryWrapper -> 0
}

public actual val PlatformFile.path: String
    get() = when (val file = webFile) {
        is WebFile.FileWrapper -> file.path
        is WebFile.DirectoryWrapper -> file.path
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
}

public actual fun PlatformFile.lastModified(): Instant = when (val file = webFile) {
    is WebFile.FileWrapper -> file.lastModified
    is WebFile.DirectoryWrapper -> file.lastModified
}

public actual fun PlatformFile.parent(): PlatformFile? = when (val file = webFile) {
    is WebFile.FileWrapper -> file.parent?.toPlatformFile()
    is WebFile.DirectoryWrapper -> file.parent?.toPlatformFile()
}

public actual fun PlatformFile.isRegularFile(): Boolean =
    webFile is WebFile.FileWrapper

public actual fun PlatformFile.isDirectory(): Boolean =
    webFile is WebFile.DirectoryWrapper

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit) {
    block(list())
}

public actual fun PlatformFile.list(): List<PlatformFile> = when (val file = webFile) {
    is WebFile.FileWrapper -> throw FileKitException("Cannot list a regular file")
    is WebFile.DirectoryWrapper -> file.children.map { it.toPlatformFile() }
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.readBytes(): ByteArray = when (val file = webFile) {
    is WebFile.FileWrapper -> withContext(Dispatchers.Main) {
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

            reader.readAsArrayBuffer(file.file)
        }
    }

    is WebFile.DirectoryWrapper -> throw FileKitException("Cannot read bytes from a directory")
}

public actual suspend fun PlatformFile.readString(): String =
    readBytes().decodeToString()

public actual suspend fun PlatformFile?.sizeRecursively(): Long {
    return sizeRecursivelyImpl(
        sanitizeSize = { it }
    )
}