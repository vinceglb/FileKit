package io.github.vinceglb.filekit.dialogs.compose.util

import androidx.annotation.IntRange
import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile

/**
 * Encodes the ImageBitmap into a ByteArray using the specified format and quality.
 *
 * @param format The desired output format (PNG or JPEG). Defaults to JPEG.
 * @param quality The compression quality (0-100) when using JPEG format. Defaults to 100.
 * @return ByteArray containing the encoded image data.
 * @throws Exception if encoding fails.
 */
public expect suspend fun ImageBitmap.encodeToByteArray(
    format: ImageFormat = ImageFormat.JPEG,
    @IntRange(from = 0, to = 100) quality: Int = 100
): ByteArray

/**
 * Converts a [PlatformFile] to an [ImageBitmap].
 *
 * @return The converted [ImageBitmap]
 * @throws Exception if the conversion fails.
 */
public expect suspend fun PlatformFile.toImageBitmap(): ImageBitmap
