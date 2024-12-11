package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path

public fun String.toPath(): Path = Path(this)

public operator fun Path.div(child: String): Path = Path(this, child)
