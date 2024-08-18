package io.github.vinceglb.filekit.core

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

public class PlatformUriFile(
    private val context: Context,
    private val wrappedFile: DocumentFile,
) : IPlatformFile {
    @Suppress("unused")
    public constructor(context: Context, file: IPlatformFile) : this(context, (file as PlatformUriFile).wrappedFile)

    @Suppress("unused")
    public constructor(context: Context, uri: Uri, isTree: Boolean) : this(
        context,
        if (isTree) {
            DocumentFile.fromTreeUri(context, uri)
        } else {
            DocumentFile.fromSingleUri(context, uri)
        }!!,
    )

    override val nameWithoutExtension: String
        get() = wrappedFile.name?.substringBeforeLast(".") ?: wrappedFile.uri.toString()

    override suspend fun getParent(): String? = wrappedFile.parentFile?.uri?.toString()
    override suspend fun getParentFile(): IPlatformFile? = wrappedFile.parentFile?.let { PlatformUriFile(context, it) }
    override suspend fun isAbsolute(): Boolean = false
    override fun getAbsolutePath(): String = path
    override fun getAbsoluteFile(): IPlatformFile = this
    override suspend fun getCanonicalPath(): String = throw IllegalAccessException("Not Supported")
    override suspend fun getCanonicalFile(): IPlatformFile = throw IllegalAccessException("Not Supported")
    override suspend fun getCanRead(): Boolean = wrappedFile.canRead()
    override suspend fun getCanWrite(): Boolean = wrappedFile.canWrite()
    override suspend fun getExists(): Boolean = wrappedFile.exists()
    override suspend fun isDirectory(): Boolean = wrappedFile.isDirectory
    override suspend fun isFile(): Boolean = wrappedFile.isFile
    override suspend fun isHidden(): Boolean = false
    override suspend fun getLastModified(): Long = wrappedFile.lastModified()
    override suspend fun getLength(): Long = wrappedFile.length()
    override suspend fun getTotalSpace(): Long = throw IllegalAccessException("Not Supported")
    override suspend fun getFreeSpace(): Long = throw IllegalAccessException("Not Supported")
    override suspend fun getUsableSpace(): Long = throw IllegalAccessException("Not Supported")

    override suspend fun createNewFile(): Boolean {
        //DocumentFile creates itself.
        return true
    }

    override suspend fun delete(): Boolean {
        return wrappedFile.delete()
    }

    override suspend fun deleteOnExit() {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun list(): Array<String> {
        return wrappedFile.listFiles().map { it.name ?: it.uri.toString() }.toTypedArray()
    }

    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String> {
        return wrappedFile.listFiles().filter {
            filter(PlatformUriFile(context, it.parentFile!!), it.name ?: it.uri.toString())
        }.map { it.name ?: it.uri.toString() }.toTypedArray()
    }

    override suspend fun listFiles(): Array<IPlatformFile> {
        return wrappedFile.listFiles().map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it.parentFile!!), it.name ?: it.uri.toString()) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile> {
        return wrappedFile.listFiles().filter { filter(PlatformUriFile(context, it)) }
            .map { PlatformUriFile(context, it) }
            .toTypedArray()
    }

    override suspend fun mkdir(): Boolean {
        return true
    }

    override suspend fun mkdirs(): Boolean {
        return true
    }

    override suspend fun renameTo(dest: IPlatformFile): Boolean {
        return wrappedFile.renameTo(dest.name)
    }

    override suspend fun setLastModified(time: Long): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadOnly(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setWritable(writable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setReadable(readable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun setExecutable(executable: Boolean): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun canExecute(): Boolean {
        throw IllegalAccessException("Not Supported")
    }

    override suspend fun openOutputStream(append: Boolean): Sink? {
        return context.contentResolver.openOutputStream(wrappedFile.uri, "w${if (append) "a" else ""}")?.asSink()?.buffered()
    }

    override suspend fun openInputStream(): Source? {
        return context.contentResolver.openInputStream(wrappedFile.uri)?.asSource()?.buffered()
    }

    override val name: String
        get() = wrappedFile.name ?: wrappedFile.uri.toString()
    override val path: String
        get() = wrappedFile.uri.toString()

    @Deprecated("Use getInputStream() instead")
    override suspend fun readBytes(): ByteArray = context.contentResolver.openInputStream(wrappedFile.uri)?.use { it.readBytes() } ?: byteArrayOf()

    @Deprecated("Use getLength() instead")
    override fun getSize(): Long = wrappedFile.length()

    override fun compareTo(other: IPlatformFile): Int {
        if (other !is PlatformUriFile) return -1

        return wrappedFile.uri.compareTo(other.wrappedFile.uri)
    }

    override fun equals(other: Any?): Boolean {
        return other is PlatformUriFile
                && wrappedFile.uri == other.wrappedFile.uri
    }

    override fun hashCode(): Int {
        return wrappedFile.uri.hashCode()
    }
}
