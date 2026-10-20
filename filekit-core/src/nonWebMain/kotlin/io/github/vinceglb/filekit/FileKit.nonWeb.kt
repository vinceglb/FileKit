package io.github.vinceglb.filekit

import androidx.annotation.IntRange
import io.github.vinceglb.filekit.utils.runSuspendCatchingFileKit

/**
 * Returns the directory for storing files on this platform.
 *
 * On Android, this corresponds to `Context.filesDir`.
 * On Apple, this corresponds to `NSApplicationSupportDirectory`.
 * On JVM, this corresponds to a platform-specific application data directory.
 */
public expect val FileKit.filesDir: PlatformFile

/**
 * Returns the directory for storing cache files on this platform.
 *
 * On Android, this corresponds to `Context.cacheDir`.
 * On Apple, this corresponds to `NSCachesDirectory`.
 * On JVM, this corresponds to a platform-specific cache directory.
 */
public expect val FileKit.cacheDir: PlatformFile

/**
 * Returns the directory for storing databases on this platform.
 *
 * This is primarily relevant for Android where it corresponds to the databases directory.
 * On other platforms, it might fall back to `filesDir` or another appropriate location.
 */
public expect val FileKit.databasesDir: PlatformFile

/**
 * Returns the project directory.
 *
 * This is typically used in desktop/server environments to refer to the root of the project.
 */
public expect val FileKit.projectDir: PlatformFile

/**
 * Saves an image to the platform's gallery or photo album.
 *
 * @param bytes The image data as a byte array.
 * @param filename The name of the file to save.
 * @return [Result.success] when the image is saved. [Result.failure] when it fails.
 */
public expect suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
): Result<Unit>

/**
 * Saves an image from a [PlatformFile] to the platform's gallery or photo album.
 *
 * @param file The [PlatformFile] containing the image.
 * @param filename The name of the file to save. Defaults to the file's name.
 * @return [Result.success] when the image is saved. [Result.failure] when it fails.
 */
public suspend fun FileKit.saveImageToGallery(
    file: PlatformFile,
    filename: String = file.name,
): Result<Unit> {
    val imageBytes = runSuspendCatchingFileKit { file.readBytes() }.getOrElse { error ->
        return Result.failure(error)
    }

    return saveImageToGallery(
        bytes = imageBytes,
        filename = filename,
    )
}

/**
 * Saves a video to the platform's gallery or video album.
 *
 * @param file The [PlatformFile] containing the video.
 * @param filename The name of the file to save.
 * @return [Result.success] when the video is saved. [Result.failure] when it fails.
 */
public expect suspend fun FileKit.saveVideoToGallery(
    file: PlatformFile,
    filename: String = file.name,
): Result<Unit>

/**
 * Compresses an image.
 *
 * @param bytes The image data to compress.
 * @param imageFormat The desired output format (JPEG or PNG). Defaults to [ImageFormat.JPEG].
 * @param quality The compression quality (0-100). Only applies to JPEG. Defaults to 80.
 * @param maxWidth The maximum width of the output image. If null, the width is unchanged.
 * @param maxHeight The maximum height of the output image. If null, the height is unchanged.
 * @return The compressed image data as a byte array.
 */
public expect suspend fun FileKit.compressImage(
    bytes: ByteArray,
    imageFormat: ImageFormat = ImageFormat.JPEG,
    @IntRange(from = 0, to = 100) quality: Int = 80,
    maxWidth: Int? = null,
    maxHeight: Int? = null,
): ByteArray

/**
 * Compresses an image from a [PlatformFile].
 *
 * @param file The [PlatformFile] containing the image.
 * @param imageFormat The desired output format (JPEG or PNG). Defaults to [ImageFormat.JPEG].
 * @param quality The compression quality (0-100). Only applies to JPEG. Defaults to 80.
 * @param maxWidth The maximum width of the output image. If null, the width is unchanged.
 * @param maxHeight The maximum height of the output image. If null, the height is unchanged.
 * @return The compressed image data as a byte array.
 */
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
    imageFormat = imageFormat,
)
