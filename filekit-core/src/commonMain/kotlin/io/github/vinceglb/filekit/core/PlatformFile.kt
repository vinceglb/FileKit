package io.github.vinceglb.filekit.core

public expect class PlatformFile {
    public val name: String
    public val path: String?

    public suspend fun readBytes(): ByteArray
    public fun getSize(): Long?
}

public val PlatformFile.baseName: String
    get() = name.substringBeforeLast(".", name)

public val PlatformFile.extension: String
    get() = name.substringAfterLast(".")

public expect class PlatformDirectory {
    public val path: String?
}

public typealias PlatformFiles = List<PlatformFile>
