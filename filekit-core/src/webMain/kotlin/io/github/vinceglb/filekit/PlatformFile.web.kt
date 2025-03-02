package io.github.vinceglb.filekit

public actual suspend fun PlatformFile.readString(): String =
    readBytes().decodeToString()
