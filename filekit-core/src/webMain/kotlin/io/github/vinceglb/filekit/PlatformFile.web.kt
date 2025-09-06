package io.github.vinceglb.filekit

public actual suspend fun PlatformFile.readString(): String =
    readBytes().decodeToString()

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}
