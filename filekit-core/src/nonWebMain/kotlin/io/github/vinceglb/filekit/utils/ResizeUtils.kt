package io.github.vinceglb.filekit.utils

import kotlin.math.roundToInt

/**
 * Calculate the new dimensions of an image based on the original dimensions and the maximum width and height.
 *
 * @param originalWidth The original width of the image.
 * @param originalHeight The original height of the image.
 * @param maxWidth The maximum width of the image.
 * @param maxHeight The maximum height of the image.
 * @return A pair containing the new width and height of the image.
 */
internal fun calculateNewDimensions(
    originalWidth: Int,
    originalHeight: Int,
    maxWidth: Int?,
    maxHeight: Int?
): Pair<Int, Int> {
    var newWidth = originalWidth
    var newHeight = originalHeight

    if (maxWidth != null && newWidth > maxWidth) {
        val aspectRatio = newHeight.toFloat() / newWidth
        newWidth = maxWidth
        newHeight = (newWidth * aspectRatio).roundToInt()
    }

    if (maxHeight != null && newHeight > maxHeight) {
        val aspectRatio = newWidth.toFloat() / newHeight
        newHeight = maxHeight
        newWidth = (newHeight * aspectRatio).roundToInt()
    }

    return newWidth to newHeight
}
