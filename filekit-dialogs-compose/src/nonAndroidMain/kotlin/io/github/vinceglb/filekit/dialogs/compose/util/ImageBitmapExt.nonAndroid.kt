package io.github.vinceglb.filekit.dialogs.compose.util

import androidx.annotation.IntRange
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Encodes the ImageBitmap into a ByteArray using the specified format and quality.
 *
 * @param format The desired output format (PNG or JPEG). Defaults to JPEG.
 * @param quality The compression quality (0-100) when using JPEG format. Defaults to 100.
 * @return ByteArray containing the encoded image data.
 * @throws Exception if encoding fails.
 */
public actual suspend fun ImageBitmap.encodeToByteArray(
    format: ImageFormat,
    @IntRange(from = 0, to = 100) quality: Int
): ByteArray = withContext(Dispatchers.Unconfined) {
    val bitmap = this@encodeToByteArray.asSkiaBitmap()
    val imageFormat = when (format) {
        ImageFormat.JPEG -> EncodedImageFormat.JPEG
        ImageFormat.PNG -> EncodedImageFormat.PNG
    }
    Image
        .makeFromBitmap(bitmap)
        .encodeToData(imageFormat, quality)
        ?.bytes ?: ByteArray(0)
}

/**
 * Converts a [PlatformFile] to an [ImageBitmap].
 *
 * @return The converted [ImageBitmap]
 * @throws Exception if the conversion fails.
 */
public actual suspend fun PlatformFile.toImageBitmap(): ImageBitmap =
    Image
        .makeFromEncoded(readBytes())
        .toComposeImageBitmap()
