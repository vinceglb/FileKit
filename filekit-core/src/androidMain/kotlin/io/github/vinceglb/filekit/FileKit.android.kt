package io.github.vinceglb.filekit

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.IntRange
import io.github.vinceglb.filekit.utils.calculateNewDimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

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

public actual suspend fun FileKit.compressPhoto(
    imageData: ByteArray,
    @IntRange(from = 0, to = 100) quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
    compressFormat: CompressFormat,
): ByteArray? = withContext(Dispatchers.IO) {
    // Step 1: Decode the ByteArray to Bitmap
    val originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        ?: return@withContext null

    // Step 2: Calculate the new dimensions while maintaining aspect ratio
    val (newWidth, newHeight) = calculateNewDimensions(
        originalBitmap.width,
        originalBitmap.height,
        maxWidth,
        maxHeight
    )

    // Step 3: Resize the Bitmap
    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

    // Step 4: Create a ByteArrayOutputStream to hold the compressed data
    val outputStream = ByteArrayOutputStream()

    // Step 5: Compress the resized Bitmap
    val format = when (compressFormat) {
        CompressFormat.JPEG -> Bitmap.CompressFormat.JPEG
        CompressFormat.PNG -> Bitmap.CompressFormat.PNG
    }
    resizedBitmap.compress(format, quality, outputStream)

    // Step 6: Convert the compressed data back to ByteArray
    outputStream.toByteArray()
}
