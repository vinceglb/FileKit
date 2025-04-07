package io.github.vinceglb.filekit.dialogs.compose.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

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
): ByteArray = withContext(Dispatchers.IO) {
    val bitmap = this@encodeToByteArray.asAndroidBitmap()
    val compressFormat = when (format) {
        ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageFormat.PNG -> Bitmap.CompressFormat.PNG
    }
    ByteArrayOutputStream().use { bytes ->
        bitmap.compress(compressFormat, quality, bytes)
        bytes.toByteArray()
    }
}

/**
 * Converts a [PlatformFile] to an [ImageBitmap].
 *
 * @return The converted [ImageBitmap]
 * @throws Exception if the conversion fails.
 */
public actual suspend fun PlatformFile.toImageBitmap(): ImageBitmap =
    BitmapFactory
        .decodeFile(this.absolutePath())
        .asImageBitmap()
