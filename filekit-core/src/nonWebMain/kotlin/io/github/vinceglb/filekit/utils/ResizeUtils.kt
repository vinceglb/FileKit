package io.github.vinceglb.filekit.utils

import kotlin.math.roundToInt

// Helper function to calculate the new dimensions while maintaining aspect ratio
internal fun calculateNewDimensions(
    originalWidth: Int,
    originalHeight: Int,
    targetWidth: Int?,
    targetHeight: Int?
): Pair<Int, Int> {
    return when {
        targetWidth != null -> {
            val aspectRatio = originalHeight.toFloat() / originalWidth
            val newHeight = (targetWidth * aspectRatio).roundToInt()
            targetWidth to newHeight
        }

        targetHeight != null -> {
            val aspectRatio = originalWidth.toFloat() / originalHeight
            val newWidth = (targetHeight * aspectRatio).roundToInt()
            newWidth to targetHeight
        }

        else -> originalWidth to originalHeight  // No resizing if no target dimensions are specified
    }
}
