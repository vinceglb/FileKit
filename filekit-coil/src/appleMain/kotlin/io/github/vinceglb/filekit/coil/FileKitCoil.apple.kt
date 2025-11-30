package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.PlatformFile

/**
 * Returns the underlying [NSURL] object for Apple platforms.
 */
internal actual val PlatformFile.underlyingFile: Any
    get() = nsUrl
