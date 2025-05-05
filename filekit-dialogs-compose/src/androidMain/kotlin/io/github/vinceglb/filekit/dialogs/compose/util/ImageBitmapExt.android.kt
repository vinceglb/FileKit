package io.github.vinceglb.filekit.dialogs.compose.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.annotation.IntRange
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

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
 * This function corrects the image orientation based on EXIF data.
 *
 * @return The converted and correctly oriented [ImageBitmap]
 * @throws Exception if the conversion fails.
 */
public actual suspend fun PlatformFile.toImageBitmap(): ImageBitmap =
    withContext(Dispatchers.IO) {
        val originalBitmap = decodeBitmap()
        val orientation = getExifOrientation()
        val rotatedBitmap = rotateBitmapIfRequired(originalBitmap, orientation)
        rotatedBitmap.asImageBitmap()
    }

private fun PlatformFile.decodeBitmap(): Bitmap {
    return when (val androidFile = androidFile) {
        is AndroidFile.FileWrapper -> BitmapFactory.decodeFile(absolutePath())
        is AndroidFile.UriWrapper -> {
            FileKit.context.contentResolver.openInputStream(androidFile.uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: throw IOException("Could not open InputStream for URI: ${androidFile.uri}")
        }
    }
}

private fun PlatformFile.getExifOrientation(): Int {
    return try {
        when (val androidFile = androidFile) {
            is AndroidFile.FileWrapper -> ExifInterface(absolutePath())
            is AndroidFile.UriWrapper -> {
                FileKit.context.contentResolver.openInputStream(androidFile.uri)
                    ?.use { inputStream ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            ExifInterface(inputStream)
                        } else {
                            // Before API 24, ExifInterface didn't support InputStream.
                            // We need the file path, which might not be available for all URIs.
                            // Attempting to get path; fallback to default orientation if unavailable.
                            val path = androidFile.uri.path // This might be null or inaccurate
                            if (path != null) {
                                ExifInterface(path)
                            } else {
                                // Cannot determine orientation from URI on older APIs without a direct path
                                return ExifInterface.ORIENTATION_NORMAL // Or throw? Return default for now.
                            }
                        }
                    }
                    ?: throw IOException("Could not open InputStream for URI to read EXIF: ${androidFile.uri}")
            }
        }.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } catch (e: Exception) {
        // Log error or handle it as needed
        println("Error reading EXIF data: ${e.message}")
        ExifInterface.ORIENTATION_NORMAL // Fallback to default orientation in case of error
    }
}

private fun rotateBitmapIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.preScale(-1.0f, 1.0f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(-90f)
            matrix.preScale(-1.0f, 1.0f)
        }

        ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> return bitmap
        else -> return bitmap // Unknown orientation, return original
    }

    return try {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            // Only recycle the original bitmap if a new one was created
            if (it != bitmap) {
                bitmap.recycle()
            }
        }
    } catch (e: OutOfMemoryError) {
        // Handle OOM, maybe return the original bitmap or scale down
        println("OutOfMemoryError rotating bitmap: ${e.message}")
        bitmap // Return original bitmap if rotation fails due to OOM
    }
}
