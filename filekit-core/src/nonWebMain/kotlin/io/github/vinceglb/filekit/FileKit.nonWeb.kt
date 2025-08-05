package io.github.vinceglb.filekit

import androidx.annotation.IntRange

public expect val FileKit.filesDir: PlatformFile

public expect val FileKit.cacheDir: PlatformFile

public expect val FileKit.databasesDir: PlatformFile

public expect val FileKit.projectDir: PlatformFile

public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
)

public suspend fun FileKit.saveImageToGallery(
    file: PlatformFile,
    filename: String = file.name,
): Unit = saveImageToGallery(
    bytes = file.readBytes(),
    filename = filename,
)

public expect suspend fun FileKit.compressImage(
    bytes: ByteArray,
    imageFormat: ImageFormat = ImageFormat.JPEG,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
): ByteArray

public suspend fun FileKit.compressImage(
    file: PlatformFile,
    imageFormat: ImageFormat = ImageFormat.JPEG,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
): ByteArray = compressImage(
    bytes = file.readBytes(),
    quality = quality,
    maxWidth = maxWidth,
    maxHeight = maxHeight,
    imageFormat = imageFormat
)

public expect fun FileKit.openFile(
    file: PlatformFile
)