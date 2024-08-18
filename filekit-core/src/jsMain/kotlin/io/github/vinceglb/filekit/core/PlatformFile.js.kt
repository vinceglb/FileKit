package io.github.vinceglb.filekit.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public actual open class PlatformFile(
    public val file: File,
) : IPlatformFile {
    public actual companion object;

    @Deprecated("Use getInputStream() instead")
    public actual override suspend fun readBytes(): ByteArray = withContext(Dispatchers.Main) {
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
                        ?: throw IllegalStateException("Could not read file")

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
            reader.readAsArrayBuffer(file)
        }
    }

    @Deprecated("Use getLength() instead")
    public actual override fun getSize(): Long? = file.size.toLong()

    actual override val nameWithoutExtension: String
        get() = file.name.substringBeforeLast(".")

    actual override suspend fun getParent(): String? = null

    actual override suspend fun getParentFile(): IPlatformFile? = null

    actual override suspend fun isAbsolute(): Boolean = true

    actual override fun getAbsolutePath(): String {
        error("Unsupported")
    }

    actual override fun getAbsoluteFile(): IPlatformFile = this

    actual override suspend fun getCanonicalPath(): String {
        error("Unsupported")
    }

    actual override suspend fun getCanonicalFile(): IPlatformFile = this

    actual override suspend fun getCanRead(): Boolean = true

    actual override suspend fun getCanWrite(): Boolean = false

    actual override suspend fun getExists(): Boolean = true

    actual override suspend fun isDirectory(): Boolean = false

    actual override suspend fun isFile(): Boolean = true

    actual override suspend fun isHidden(): Boolean = false

    actual override suspend fun getLastModified(): Long = file.lastModified.toLong()

    actual override suspend fun getLength(): Long = file.size.toLong()

    actual override suspend fun getTotalSpace(): Long {
        error("Unsupported")
    }

    actual override suspend fun getFreeSpace(): Long {
        error("Unsupported")
    }

    actual override suspend fun getUsableSpace(): Long {
        error("Unsupported")
    }

    actual override suspend fun createNewFile(): Boolean = true

    actual override suspend fun delete(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun deleteOnExit() {
        error("Unsupported")
    }

    actual override suspend fun list(): Array<String>? {
        error("Unsupported")
    }

    actual override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? {
        error("Unsupported")
    }

    actual override suspend fun listFiles(): Array<IPlatformFile>? {
        error("Unsupported")
    }

    actual override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? {
        error("Unsupported")
    }

    actual override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? {
        error("Unsupported")
    }

    actual override suspend fun mkdir(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun mkdirs(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun renameTo(dest: IPlatformFile): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setLastModified(time: Long): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadOnly(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setWritable(writable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadable(readable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setExecutable(executable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun canExecute(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun openOutputStream(append: Boolean): Sink? {
        error("Unsupported")
    }

    actual override suspend fun openInputStream(): Source? {
        @Suppress("DEPRECATION")
        val bytes = readBytes()
        val buffer = Buffer()

        buffer.write(bytes)

        return buffer
    }

    actual override fun hashCode(): Int = file.hashCode()

    actual override fun equals(other: Any?): Boolean = other is PlatformFile && other.file == file

    actual override fun compareTo(other: IPlatformFile): Int = name.compareTo(other.name)

    public actual override val name: String
        get() = file.name

    public actual override val path: String?
        get() = null

    public actual constructor(pathName: String) : this(File(arrayOf(), "")) {
        error("Unsupported")
    }

    public actual constructor(parent: String, child: String) : this(File(arrayOf(), "")) {
        error("Unsupported")
    }

    public actual constructor(
        parent: PlatformFile,
        child: String
    ) : this(File(arrayOf(), "")) {
        error("Unsupported")
    }
}
