package io.github.vinceglb.filekit.utils

import kotlinx.io.files.Path

/**
 * Converts this string to a [Path].
 *
 * @return A [Path] instance representing this string.
 */
public fun String.toPath(): Path = Path(this)

/**
 * Appends a child path to this [Path].
 *
 * @param child The child path string.
 * @return A [Path] instance representing the combined path.
 */
public operator fun Path.div(child: String): Path = Path(this, child)
