package io.github.vinceglb.filekit.core

import kotlinx.io.Sink
import kotlinx.io.Source

public expect open class PlatformFile : IPlatformFile {
    public companion object;

    public constructor(pathName: String)
    public constructor(parent: String, child: String)
    public constructor(parent: PlatformFile, child: String)

    override val nameWithoutExtension: String

    override suspend fun getParent(): String?
    override suspend fun getParentFile(): IPlatformFile?
    override suspend fun isAbsolute(): Boolean
    override fun getAbsolutePath(): String
    override fun getAbsoluteFile(): IPlatformFile
    override suspend fun getCanonicalPath(): String
    override suspend fun getCanonicalFile(): IPlatformFile
    override suspend fun getCanRead(): Boolean
    override suspend fun getCanWrite(): Boolean
    override suspend fun getExists(): Boolean
    override suspend fun isDirectory(): Boolean
    override suspend fun isFile(): Boolean
    override suspend fun isHidden(): Boolean
    override suspend fun getLastModified(): Long
    override suspend fun getLength(): Long
    override suspend fun getTotalSpace(): Long
    override suspend fun getFreeSpace(): Long
    override suspend fun getUsableSpace(): Long

    override suspend fun createNewFile(): Boolean
    override suspend fun delete(): Boolean
    override suspend fun deleteOnExit()
    override suspend fun list(): Array<String>?
    override suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>?
    override suspend fun listFiles(): Array<IPlatformFile>?
    override suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>?
    override suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>?
    override suspend fun mkdir(): Boolean
    override suspend fun mkdirs(): Boolean
    override suspend fun renameTo(dest: IPlatformFile): Boolean
    override suspend fun setLastModified(time: Long): Boolean
    override suspend fun setReadOnly(): Boolean
    override suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    override suspend fun setWritable(writable: Boolean): Boolean
    override suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    override suspend fun setReadable(readable: Boolean): Boolean
    override suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    override suspend fun setExecutable(executable: Boolean): Boolean
    override suspend fun canExecute(): Boolean

    override suspend fun openOutputStream(append: Boolean): Sink?
    override suspend fun openInputStream(): Source?

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
    override fun compareTo(other: IPlatformFile): Int

    public override val name: String
    public override val path: String?

    @Deprecated("Use getInputStream() instead")
    public override suspend fun readBytes(): ByteArray
    @Deprecated("Use getLength() instead")
    public override fun getSize(): Long?
}

public operator fun PlatformFile.Companion.invoke(pathName: String): PlatformFile {
    return PlatformFile(pathName)
}

public operator fun PlatformFile.Companion.invoke(parent: String, child: String): PlatformFile {
    return PlatformFile(parent, child)
}

public operator fun PlatformFile.Companion.invoke(parent: PlatformFile, child: String): PlatformFile {
    return PlatformFile(parent, child)
}

/**
 * The base File representation for Platform Files to
 * override if needed.
 */
public interface IPlatformFile : Comparable<IPlatformFile> {
    public val nameWithoutExtension: String

    public suspend fun getParent(): String?
    public suspend fun getParentFile(): IPlatformFile?
    public suspend fun isAbsolute(): Boolean
    public fun getAbsolutePath(): String
    public fun getAbsoluteFile(): IPlatformFile
    public suspend fun getCanonicalPath(): String
    public suspend fun getCanonicalFile(): IPlatformFile
    public suspend fun getCanRead(): Boolean
    public suspend fun getCanWrite(): Boolean
    public suspend fun getExists(): Boolean
    public suspend fun isDirectory(): Boolean
    public suspend fun isFile(): Boolean
    public suspend fun isHidden(): Boolean
    public suspend fun getLastModified(): Long
    public suspend fun getLength(): Long
    public suspend fun getTotalSpace(): Long
    public suspend fun getFreeSpace(): Long
    public suspend fun getUsableSpace(): Long

    public suspend fun createNewFile(): Boolean
    public suspend fun delete(): Boolean
    public suspend fun deleteOnExit()
    public suspend fun list(): Array<String>?
    public suspend fun list(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<String>?
    public suspend fun listFiles(): Array<IPlatformFile>?
    public suspend fun listFiles(filter: (dir: IPlatformFile, name: String) -> Boolean): Array<IPlatformFile>?
    public suspend fun listFiles(filter: (pathName: IPlatformFile) -> Boolean): Array<IPlatformFile>?
    public suspend fun mkdir(): Boolean
    public suspend fun mkdirs(): Boolean
    public suspend fun renameTo(dest: IPlatformFile): Boolean
    public suspend fun setLastModified(time: Long): Boolean
    public suspend fun setReadOnly(): Boolean
    public suspend fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    public suspend fun setWritable(writable: Boolean): Boolean
    public suspend fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    public suspend fun setReadable(readable: Boolean): Boolean
    public suspend fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    public suspend fun setExecutable(executable: Boolean): Boolean
    public suspend fun canExecute(): Boolean

    public suspend fun openOutputStream(append: Boolean = false): Sink?
    public suspend fun openInputStream(): Source?

    public val name: String
    public val path: String?

    @Deprecated("Use getInputStream() instead")
    public suspend fun readBytes(): ByteArray
    @Deprecated("Use getLength() instead")
    public fun getSize(): Long?

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
    override fun compareTo(other: IPlatformFile): Int
}

public val IPlatformFile.baseName: String
    get() = name.substringBeforeLast(".")

public val IPlatformFile.extension: String
    get() = name.substringAfterLast(".")

@Deprecated("Use PlatformFile directly instead")
public class PlatformDirectory : PlatformFile {
    public constructor(pathName: String) : super(pathName)
    public constructor(parent: String, child: String) : super(parent, child)
    public constructor(parent: PlatformFile, child: String) : super(parent, child)
}

public typealias PlatformFiles = List<IPlatformFile>
