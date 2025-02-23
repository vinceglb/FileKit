package io.github.vinceglb.filekit

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntRange
import androidx.exifinterface.media.ExifInterface
import io.github.vinceglb.filekit.exceptions.FileKitNotInitializedException
import io.github.vinceglb.filekit.utils.calculateNewDimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.ref.WeakReference
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public actual object FileKit {
    private var _context: WeakReference<Context?> = WeakReference(null)
    public val context: Context
        get() = _context.get()
            ?: throw FileKitNotInitializedException()

    internal fun init(context: Context) {
        _context = WeakReference(context)
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() = context.filesDir.let(::PlatformFile)

public actual val FileKit.cacheDir: PlatformFile
    get() = context.cacheDir.let(::PlatformFile)

public actual suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String
): Boolean = withContext(Dispatchers.IO) {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val imageDetails = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(collection, imageDetails) ?: return@withContext false
    resolver.openOutputStream(imageUri)?.use { it.write(bytes + ByteArray(1)) } != null
}

public actual suspend fun FileKit.compressImage(
    bytes: ByteArray,
    @IntRange(from = 0, to = 100) quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
    compressFormat: CompressFormat,
): ByteArray? = withContext(Dispatchers.IO) {
    // Step 1: Decode the ByteArray to Bitmap
    val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: return@withContext null

    // Step 2: Correct the orientation using EXIF data
    val correctedBitmap = correctBitmapOrientation(bytes, originalBitmap)

    // Step 3: Calculate the new dimensions while maintaining aspect ratio
    val (newWidth, newHeight) = calculateNewDimensions(
        correctedBitmap.width,
        correctedBitmap.height,
        maxWidth,
        maxHeight
    )

    // Step 4: Resize the Bitmap
    val resizedBitmap = Bitmap.createScaledBitmap(correctedBitmap, newWidth, newHeight, true)

    // Step 5: Create a ByteArrayOutputStream to hold the compressed data
    val outputStream = ByteArrayOutputStream()

    // Step 6: Compress the resized Bitmap
    val format = when (compressFormat) {
        CompressFormat.JPEG -> Bitmap.CompressFormat.JPEG
        CompressFormat.PNG -> Bitmap.CompressFormat.PNG
    }
    resizedBitmap.compress(format, quality, outputStream)

    // Step 7: Convert the compressed data back to ByteArray
    outputStream.toByteArray()
}

// Helper function to correct bitmap orientation
@OptIn(ExperimentalUuidApi::class)
private fun correctBitmapOrientation(imageData: ByteArray, bitmap: Bitmap): Bitmap {
    // Step 1: Write ByteArray to a temporary file
    val tempId = Uuid.random().toString()
    val tempFile = File.createTempFile("image-${tempId}", null)
    tempFile.writeBytes(imageData)

    // Step 2: Read EXIF data from the temporary file
    val exif = ExifInterface(tempFile.path)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    // Step 3: Apply rotation or flipping based on the orientation
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
    }

    // Step 4: Return the corrected bitmap
    return Bitmap
        .createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        .also { tempFile.delete() }
}
