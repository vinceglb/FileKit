package io.github.vinceglb.filekit.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import platform.Foundation.NSData
import platform.Foundation.NSDataReadingUncached
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLAttributeModificationDateKey
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.lastPathComponent
import platform.Foundation.pathComponents
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy

@ExperimentalForeignApi
public actual open class PlatformFile(public val nsUrl: NSURL) : IPlatformFile {
    public actual companion object;

    actual override val nameWithoutExtension: String
        get() = (nsUrl.lastPathComponent ?: "").substringBeforeLast(".")

    actual override suspend fun getParent(): String? = (nsUrl.absoluteURL ?: nsUrl).pathComponents
        ?.takeIf { it.isNotEmpty() }
        ?.run { take(size - 1) }
        ?.takeIf { it.size < (nsUrl.pathComponents?.size ?: 0) }
        ?.joinToString("$SystemPathSeparator")

    actual override suspend fun getParentFile(): IPlatformFile? = getParent()?.let { PlatformFile(it) }

    actual override suspend fun isAbsolute(): Boolean = nsUrl.absoluteURL?.path == nsUrl.path

    actual override fun getAbsolutePath(): String = (nsUrl.absoluteURL?.path ?: nsUrl.path) ?: ""

    actual override fun getAbsoluteFile(): IPlatformFile = PlatformFile(getAbsolutePath())

    actual override suspend fun getCanonicalPath(): String = getAbsolutePath()

    actual override suspend fun getCanonicalFile(): IPlatformFile = getAbsoluteFile()

    actual override suspend fun getCanRead(): Boolean = NSFileManager.defaultManager.isReadableFileAtPath(getAbsolutePath())

    actual override suspend fun getCanWrite(): Boolean = NSFileManager.defaultManager.isWritableFileAtPath(getAbsolutePath())

    actual override suspend fun getExists(): Boolean = NSFileManager.defaultManager.fileExistsAtPath(getAbsolutePath())

    actual override suspend fun isDirectory(): Boolean = nsUrl.hasDirectoryPath

    actual override suspend fun isFile(): Boolean = !nsUrl.fileURL

    actual override suspend fun isHidden(): Boolean = false

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun getLastModified(): Long = memScoped {
        nsUrl.path?.let {
            val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
                alloc<ObjCObjectVar<NSError?>>().ptr
            (NSFileManager.defaultManager.attributesOfItemAtPath(it, errorPointer)?.get(NSURLAttributeModificationDateKey) as NSDate?)?.timeIntervalSince1970?.toLong()
        } ?: 0
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun getLength(): Long = memScoped {
        val valuePointer: CPointer<ObjCObjectVar<Any?>> = alloc<ObjCObjectVar<Any?>>().ptr
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        nsUrl.getResourceValue(valuePointer, NSURLFileSizeKey, errorPointer)
        return valuePointer.pointed.value as? Long? ?: 0
    }

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

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun delete(): Boolean = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        NSFileManager.defaultManager.removeItemAtURL(nsUrl, errorPointer)
    }

    actual override suspend fun deleteOnExit() {}

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun list(): Array<String>? = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(getAbsolutePath(), errorPointer)?.map {
            val item = (it as NSString)
            "${getAbsolutePath()}/$item"
        }?.toTypedArray()
    }

    actual override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>? = list()?.filter { filter(getAbsoluteFile(), it) }?.toTypedArray()

    actual override suspend fun listFiles(): Array<IPlatformFile>? = list()?.map { PlatformFile(it) }?.toTypedArray()

    actual override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>? = list(filter)?.map { PlatformFile(it) }?.toTypedArray()

    actual override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>? = listFiles()?.filter { filter(it) }?.toTypedArray()

    actual override suspend fun mkdir(): Boolean = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        NSFileManager.defaultManager.createDirectoryAtPath(getAbsolutePath(), false, null, errorPointer)
    }

    actual override suspend fun mkdirs(): Boolean = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        NSFileManager.defaultManager.createDirectoryAtPath(getAbsolutePath(), true, null, errorPointer)
    }

    actual override suspend fun renameTo(dest: IPlatformFile): Boolean = memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
            alloc<ObjCObjectVar<NSError?>>().ptr
        NSFileManager.defaultManager.moveItemAtPath(getAbsolutePath(), dest.getAbsolutePath(), errorPointer)
    }

    actual override suspend fun setLastModified(time: Long): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadOnly(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setWritable(
        writable: Boolean,
        ownerOnly: Boolean
    ): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setWritable(writable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadable(
        readable: Boolean,
        ownerOnly: Boolean
    ): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setReadable(readable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setExecutable(
        executable: Boolean,
        ownerOnly: Boolean
    ): Boolean {
        error("Unsupported")
    }

    actual override suspend fun setExecutable(executable: Boolean): Boolean {
        error("Unsupported")
    }

    actual override suspend fun canExecute(): Boolean {
        error("Unsupported")
    }

    actual override suspend fun openOutputStream(append: Boolean): Sink? = SystemFileSystem.sink(Path(getAbsolutePath()), append).buffered()

    actual override suspend fun openInputStream(): Source? = SystemFileSystem.source(Path(getAbsolutePath())).buffered()

    actual override fun hashCode(): Int = nsUrl.hash().toInt()

    actual override fun equals(other: Any?): Boolean = other is PlatformFile && getAbsolutePath() == other.getAbsolutePath()

    actual override fun compareTo(other: IPlatformFile): Int = getAbsolutePath().compareTo(other.getAbsolutePath())

    public actual override val name: String
        get() = nsUrl.lastPathComponent ?: ""

    public actual override val path: String?
        get() = nsUrl.path

    @Deprecated("Use getInputStream() instead")
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    public actual override suspend fun readBytes(): ByteArray = withContext(Dispatchers.IO) {
        memScoped {
            // Start accessing the security scoped resource
            nsUrl.startAccessingSecurityScopedResource()

            // Read the data
            val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
            val nsData = NSData.dataWithContentsOfURL(nsUrl, NSDataReadingUncached, error)
                ?: throw IllegalStateException("Failed to read data from $nsUrl. Error: ${error.pointed.value}")

            // Stop accessing the security scoped resource
            nsUrl.stopAccessingSecurityScopedResource()

            // Copy the data to a ByteArray
            ByteArray(nsData.length.toInt()).apply {
                memcpy(this.refTo(0), nsData.bytes, nsData.length)
            }
        }
    }

    @Deprecated("Use getLength() instead")
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    public actual override fun getSize(): Long? {
        memScoped {
            val valuePointer: CPointer<ObjCObjectVar<Any?>> = alloc<ObjCObjectVar<Any?>>().ptr
            val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
                alloc<ObjCObjectVar<NSError?>>().ptr
            nsUrl.getResourceValue(valuePointer, NSURLFileSizeKey, errorPointer)
            return valuePointer.pointed.value as? Long?
        }
    }

    public actual constructor(pathName: String) : this(NSURL.fileURLWithPath(pathName))

    public actual constructor(parent: String, child: String) : this(NSURL.fileURLWithPath("${parent}${SystemPathSeparator}${child}"))

    public actual constructor(
        parent: PlatformFile,
        child: String
    ) : this(NSURL.fileURLWithPath("${parent.getAbsolutePath()}${SystemPathSeparator}${child}"))
}
