package io.github.vinceglb.filekit

public expect class PlatformFile {
    override fun toString(): String
}

public expect val PlatformFile.name: String

public expect val PlatformFile.extension: String

public expect val PlatformFile.nameWithoutExtension: String

public expect fun PlatformFile.size(): Long

public expect suspend fun PlatformFile.readBytes(): ByteArray

public expect suspend fun PlatformFile.readString(): String
