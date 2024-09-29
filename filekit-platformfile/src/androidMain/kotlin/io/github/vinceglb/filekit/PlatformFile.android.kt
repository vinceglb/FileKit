package io.github.vinceglb.filekit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

public actual data class PlatformFile(
    val uri: Uri,
    internal val context: Context,
)

public actual val PlatformFile.underlyingFile: Any
    get() = uri

public actual val PlatformFile.name: String
    get() = context.getFileName(uri) ?: throw IllegalStateException("Failed to get file name")

public actual val PlatformFile.path: String
    get() = context.contentResolver.let {
        it.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst()
            val name = cursor.getString(nameIndex)
            File(context.filesDir, name).path
        }
    } ?: throw IllegalStateException("Failed to get file path")

public actual val PlatformFile.size: Long
    get() = runCatching {
        context.contentResolver.query(uri, null, null, null, null)
            ?.use { cursor ->
                cursor.moveToFirst()
                cursor.getColumnIndex(OpenableColumns.SIZE).let(cursor::getLong)
            }
    }.getOrNull() ?: throw IllegalStateException("Failed to get file size")

public actual suspend fun PlatformFile.readBytes(): ByteArray = withContext(Dispatchers.IO) {
    context
        .contentResolver
        .openInputStream(uri)
        .use { stream -> stream?.readBytes() }
        ?: throw IllegalStateException("Failed to read file")
}

public actual fun PlatformFile.getStream(): PlatformInputStream {
    return context
        .contentResolver
        .openInputStream(uri)
        ?.let { PlatformInputStream(it) }
        ?: throw IllegalStateException("Failed to open stream")
}

private fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor
            .getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            .let(cursor::getString)
    }
}.getOrNull()
