package io.github.vinceglb.filekit

public actual val PlatformFile.absolutePath: String
    get() = androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.UriWrapper -> androidFile.uri.path ?: ""
            is AndroidFile.FileWrapper -> androidFile.file.absolutePath
        }
    }
