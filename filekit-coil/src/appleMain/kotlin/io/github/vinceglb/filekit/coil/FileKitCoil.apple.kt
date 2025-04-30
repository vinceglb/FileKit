package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.PlatformFile

internal actual val PlatformFile.underlyingFile: Any
    get() = nsUrl
