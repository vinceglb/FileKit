package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.Blob
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsName
import kotlin.js.JsNumber
import kotlin.js.definedExternally
import kotlin.js.unsafeCast
import kotlin.time.Instant

public interface WebFileHandle {
    public val name: String
    public val type: String
    public val size: Long
    public val isDirectory: Boolean
    public val isRegularFile: Boolean
    public val lastModified: Instant
    public val path: String

    public fun getFile(): FileExt

    public fun getParent(): WebFileHandle?

    public fun list(): List<WebFileHandle>
}

public fun WebFileHandle.toPlatformFile(): PlatformFile = PlatformFile(this)

/**
 * Represents a file on the Web platform.
 * @property fh An implementation of the WasmFileHandle.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual data class PlatformFile(
    internal val fh: WebFileHandle,
) {
    @Deprecated("Please do not use this anymore to create an instance.")
    @OptIn(ExperimentalWasmJsInterop::class)
    public constructor(file: File) : this(FileHandleFile(file.unsafeCast<FileExt>()))

    @OptIn(ExperimentalWasmJsInterop::class)
    public val file: File
        get() = fh.getFile().unsafeCast<File>()

    public actual override fun toString(): String = name

    public actual companion object
}

public actual val PlatformFile.name: String
    get() = fh.name

public actual val PlatformFile.extension: String
    get() = name.substringAfterLast(".", "")

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

public actual fun PlatformFile.size(): Long =
    fh.size

public actual val PlatformFile.path: String
    get() = fh.path

public actual fun PlatformFile.mimeType(): MimeType? =
    takeIf { fh.type.isNotBlank() }
        ?.let { MimeType.parse(fh.type) }

public actual fun PlatformFile.lastModified(): Instant =
    fh.lastModified

public actual fun PlatformFile.parent(): PlatformFile? =
    fh.getParent()?.toPlatformFile()

public actual fun PlatformFile.isRegularFile(): Boolean =
    fh.isRegularFile

public actual fun PlatformFile.isDirectory(): Boolean =
    fh.isDirectory

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit) {
    block(list())
}

public actual fun PlatformFile.list(): List<PlatformFile> =
    fh.list().map { it.toPlatformFile() }

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.readBytes(): ByteArray = withContext(Dispatchers.Main) {
    suspendCoroutine { continuation ->
        val reader = FileReader()
        reader.onload = { event ->
            try {
                // Read the file as an ArrayBuffer
                val arrayBuffer = event
                    .target
                    ?.unsafeCast<FileReader>()
                    ?.result
                    ?.unsafeCast<ArrayBuffer>()
                    ?: throw FileKitException("Could not read file")

                // Convert the ArrayBuffer to a ByteArray
                val bytes = Uint8Array(arrayBuffer)

                // Copy the bytes into a ByteArray
                val byteArray = ByteArray(bytes.length)
                for (i in 0 until bytes.length) {
                    byteArray[i] = bytes[i]
                }

                // Return the ByteArray
                continuation.resume(byteArray)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }

        // Read the file as an ArrayBuffer
        reader.readAsArrayBuffer(fh.getFile().unsafeCast<File>())
    }
}

public actual suspend fun PlatformFile.readString(): String =
    readBytes().decodeToString()

/**
 *
 */
@OptIn(ExperimentalWasmJsInterop::class)
@JsName("File")
public open external class FileExt(
    fileBits: JsArray<JsAny?>, // BufferSource|Blob|String
    fileName: String,
    options: FilePropertyBag = definedExternally,
) : Blob,
    JsAny {
    public val name: String
    public val lastModified: JsNumber
    public val webkitRelativePath: String?
}
