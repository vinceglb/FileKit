package io.github.vinceglb.filekit

public actual val PlatformFile.absolutePath: String
    get() = nsUrl.path ?: ""
