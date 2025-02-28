package io.github.vinceglb.filekit

import androidx.annotation.IntRange

public expect val FileKit.filesDir: PlatformFile

public expect val FileKit.cacheDir: PlatformFile

public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
): Unit

public suspend fun FileKit.saveImageToGallery(
    file: PlatformFile,
    filename: String,
): Unit = saveImageToGallery(
    bytes = file.readBytes(),
    filename = filename,
)

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
): ByteArray? = compressImage(
    bytes = file.readBytes(),
    quality = quality,
    maxWidth = maxWidth,
    maxHeight = maxHeight,
    compressFormat = compressFormat
)

public enum class CompressFormat {
    JPEG,
    PNG,
}
