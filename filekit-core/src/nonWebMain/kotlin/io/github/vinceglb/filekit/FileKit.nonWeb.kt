package io.github.vinceglb.filekit

import androidx.annotation.IntRange

public expect val FileKit.filesDir: PlatformFile

public expect val FileKit.cacheDir: PlatformFile

public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
): Boolean

// TODO: Add compressImage with platformFile instead of ByteArray
public expect suspend fun FileKit.compressImage(
    imageData: ByteArray,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
    compressFormat: CompressFormat = CompressFormat.JPEG,
): ByteArray?

public enum class CompressFormat {
    JPEG,
    PNG,
}
