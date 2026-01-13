package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.mimeType

internal expect fun createPlatformFileForPreviews(name: String): PlatformFile

internal expect fun PlatformFile.isDirectory(): Boolean

internal fun PlatformFile.isImageFile(): Boolean {
    if (mimeType()?.primaryType?.lowercase() == "image") {
        return true
    }

    val ext = extension.lowercase()
    val imageExtensions = setOf(
        "apng",
        "avif",
        "bmp",
        "gif",
        "heic",
        "heif",
        "jpg",
        "jpeg",
        "png",
        "svg",
        "tif",
        "tiff",
        "webp",
    )

    return ext in imageExtensions
}
