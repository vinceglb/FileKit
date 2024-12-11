package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path
import platform.Foundation.NSURL

public fun NSURL.toKotlinxPath(): Path? =
    this.path?.let { Path(it) }
