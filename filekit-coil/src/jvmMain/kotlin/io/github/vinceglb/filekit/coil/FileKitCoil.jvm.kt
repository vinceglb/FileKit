package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.PlatformFile

/**
 * Returns the underlying [java.io.File] object for JVM.
 */
internal actual val PlatformFile.underlyingFile: Any
    get() = file
