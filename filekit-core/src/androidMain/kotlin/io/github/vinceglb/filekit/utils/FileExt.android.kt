package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path
import java.io.File

/**
 * Converts a [File] to a [Path].
 *
 * @return A [Path] instance representing this file.
 */
public fun File.toKotlinxPath(): Path = Path(this.path)
