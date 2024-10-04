package io.github.vinceglb.filekit

public expect class PlatformFile

public expect val PlatformFile.underlyingFile: Any

public expect val PlatformFile.name: String

public expect val PlatformFile.size: Long

public expect suspend fun PlatformFile.readBytes(): ByteArray

public val PlatformFile.baseName: String
    get() = name.substringBeforeLast(".", name)

public val PlatformFile.extension: String
    get() = name.substringAfterLast(".")
