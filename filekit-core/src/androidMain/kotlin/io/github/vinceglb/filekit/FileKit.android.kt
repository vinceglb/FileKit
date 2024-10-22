package io.github.vinceglb.filekit

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    baseName: String,
    extension: String
): Boolean = withContext(Dispatchers.IO) {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val imageDetails = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$baseName.$extension")
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(collection, imageDetails) ?: return@withContext false
    resolver.openOutputStream(imageUri)?.use { it.write(bytes + ByteArray(1)) } != null
}
