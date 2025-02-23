package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.exceptions.FileKitNSURLNullPathException
import kotlinx.io.files.Path
import platform.Foundation.NSURL

public fun NSURL.toKotlinxPath(): Path = this.path
    ?.let(::Path)
    ?: throw FileKitNSURLNullPathException()
