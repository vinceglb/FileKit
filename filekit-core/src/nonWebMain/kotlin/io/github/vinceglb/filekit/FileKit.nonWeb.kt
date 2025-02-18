package io.github.vinceglb.filekit

import androidx.annotation.IntRange

public expect val FileKit.filesDir: PlatformFile

public expect val FileKit.cacheDir: PlatformFile

public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
): Boolean

public suspend fun FileKit.saveImageToGallery(
    file: PlatformFile,
    filename: String,
): Boolean = file.readBytes()
    ?.let { saveImageToGallery(it, filename) }
    ?: false

public expect suspend fun FileKit.compressImage(
    bytes: ByteArray,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
    compressFormat: CompressFormat = CompressFormat.JPEG,
): ByteArray?

public suspend fun FileKit.compressImage(
    file: PlatformFile,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
    compressFormat: CompressFormat = CompressFormat.JPEG,
): ByteArray? = file.readBytes()
    ?.let { compressImage(it, quality, maxWidth, maxHeight, compressFormat) }

public enum class CompressFormat {
    JPEG,
    PNG,
}
