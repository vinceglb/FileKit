package io.github.vinceglb.filekit.coil

import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile

internal actual val PlatformFile.underlyingFile: Any
    get() = androidFile.let {
        when (it) {
            is AndroidFile.FileWrapper -> it.file
            is AndroidFile.UriWrapper -> it.uri
        }
    }
