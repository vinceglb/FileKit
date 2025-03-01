package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path
import java.io.File

public fun File.toKotlinxIoPath(): Path = Path(this.path)

public fun Path.toFile(): File = File(this.toString())
