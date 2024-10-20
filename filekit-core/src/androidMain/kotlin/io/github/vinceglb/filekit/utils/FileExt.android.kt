package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path
import java.io.File

public fun File.toKotlinxPath(): Path = Path(this.absolutePath)
