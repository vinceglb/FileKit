package io.github.vinceglb.filekit

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitUriPathNotSupportedException
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

public actual data class PlatformFile(
    val androidFile: AndroidFile
) {
    public actual override fun toString(): String = path

    public actual companion object
}

public sealed class AndroidFile {
    public data class FileWrapper(val file: File) : AndroidFile()
    public data class UriWrapper(val uri: Uri) : AndroidFile()
}

public actual fun PlatformFile(path: Path): PlatformFile {
    // Convert the Path back to a String to inspect its prefix. On Android, if the
    // string represents a `content://` (or other URI based) path we should treat
    // it as a real URI instead of a File system path.
    val rawPath = path.toString()

    // Heuristic: when the path contains a scheme (e.g. "content://", "file://")
    // that Android recognises as an URI, build an UriWrapper, otherwise fallback
    // to a classic FileWrapper.
    return if (rawPath.startsWith("content://", ignoreCase = true) ||
        rawPath.startsWith("file://", ignoreCase = true)) {
        PlatformFile(AndroidFile.UriWrapper(Uri.parse(rawPath)))
    } else {
        PlatformFile(AndroidFile.FileWrapper(File(rawPath)))
    }
}

public fun PlatformFile(uri: Uri): PlatformFile =
    PlatformFile(AndroidFile.UriWrapper(uri))

public fun PlatformFile(file: File): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(file))

public actual fun PlatformFile(path: String): PlatformFile {
    // If the path looks like an Android Uri ("content://" or "file://" scheme),
    // parse it accordingly, otherwise treat it as a regular filesystem path.
    return if (path.startsWith("content://", ignoreCase = true) ||
        path.startsWith("file://", ignoreCase = true)) {
        PlatformFile(AndroidFile.UriWrapper(Uri.parse(path)))
    } else {
        PlatformFile(AndroidFile.FileWrapper(File(path)))
    }
}

public actual fun PlatformFile.toKotlinxIoPath(): Path = when (androidFile) {
    is AndroidFile.FileWrapper -> androidFile.file.toKotlinxPath()
    is AndroidFile.UriWrapper -> throw FileKitUriPathNotSupportedException()
}

public actual val PlatformFile.name: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> toKotlinxIoPath().name
        is AndroidFile.UriWrapper -> getUriFileName(androidFile.uri)
    }

public actual val PlatformFile.extension: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.extension
        is AndroidFile.UriWrapper -> when {
            isDirectory() -> ""
            else -> getUriFileName(androidFile.uri).substringAfterLast(".", "")
        }
    }

public actual val PlatformFile.nameWithoutExtension: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.nameWithoutExtension
        is AndroidFile.UriWrapper -> when {
            isDirectory() -> getUriFileName(androidFile.uri)
            else -> getUriFileName(androidFile.uri).substringBeforeLast(".", "")
        }
    }

public actual val PlatformFile.path: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> toKotlinxIoPath().toString()
        is AndroidFile.UriWrapper -> androidFile.uri.toString()
    }

public actual fun PlatformFile.isRegularFile(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isRegularFile
        ?: false

    is AndroidFile.UriWrapper -> DocumentFile.fromSingleUri(
        FileKit.context,
        androidFile.uri
    )?.isFile == true
}

public actual fun PlatformFile.isDirectory(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isDirectory
        ?: false

    is AndroidFile.UriWrapper -> try {
        DocumentFile.fromTreeUri(
            FileKit.context,
            androidFile.uri
        )?.isDirectory == true
    } catch (e: Exception) {
        false
    }
}

public actual fun PlatformFile.isAbsolute(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> toKotlinxIoPath().isAbsolute
    is AndroidFile.UriWrapper -> true
}

public actual fun PlatformFile.exists(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.exists(toKotlinxIoPath())
    is AndroidFile.UriWrapper -> getDocumentFile(androidFile.uri)?.exists() == true
}

public actual fun PlatformFile.size(): Long = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.size ?: -1
    is AndroidFile.UriWrapper -> getUriFileSize(androidFile.uri) ?: -1
}

public actual fun PlatformFile.parent(): PlatformFile? = when (androidFile) {
    is AndroidFile.FileWrapper -> toKotlinxIoPath().parent?.let(::PlatformFile)
    is AndroidFile.UriWrapper -> {
        val uri = androidFile.uri
        val parentUri = uri.buildUpon()
            .path(uri.path?.substringBeforeLast('/'))
            .build()

        DocumentFile.fromTreeUri(FileKit.context, parentUri)?.let {
            PlatformFile(it.uri)
        }
    }
}

public actual fun PlatformFile.absolutePath(): String = when (androidFile) {
    is AndroidFile.FileWrapper -> androidFile.file.absolutePath
    is AndroidFile.UriWrapper -> androidFile.uri.toString()
}

public actual fun PlatformFile.absoluteFile(): PlatformFile = when (androidFile) {
    is AndroidFile.FileWrapper -> PlatformFile(SystemFileSystem.resolve(toKotlinxIoPath()))
    is AndroidFile.UriWrapper -> this
}

public actual fun PlatformFile.source(): RawSource = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.source(toKotlinxIoPath())

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openInputStream(androidFile.uri)
        ?.asSource()
        ?: throw FileKitException("Could not open input stream for Uri")
}

public actual fun PlatformFile.sink(append: Boolean): RawSink = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.sink(toKotlinxIoPath(), append)

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openOutputStream(androidFile.uri)
        ?.asSink()
        ?: throw FileKitException("Could not open output stream for Uri")
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit) {
    when (androidFile) {
        is AndroidFile.FileWrapper -> {
            val directoryFiles = SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
            block(directoryFiles)
        }

        is AndroidFile.UriWrapper -> {
            val documentFile = DocumentFile.fromTreeUri(FileKit.context, androidFile.uri)
                ?: throw FileKitException("Could not access Uri as DocumentFile")
            val files = documentFile.listFiles().map { PlatformFile(it.uri) }
            block(files)
        }
    }
}

public actual fun PlatformFile.list(): List<PlatformFile> = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)

    is AndroidFile.UriWrapper -> {
        val documentFile = DocumentFile.fromTreeUri(FileKit.context, androidFile.uri)
            ?: throw FileKitException("Could not access Uri as DocumentFile")
        documentFile.listFiles().map { PlatformFile(it.uri) }
    }
}

public actual suspend fun PlatformFile.atomicMove(destination: PlatformFile): Unit =
    withContext(Dispatchers.IO) {
        when {
            androidFile is AndroidFile.FileWrapper && destination.androidFile is AndroidFile.FileWrapper -> {
                SystemFileSystem.atomicMove(
                    source = toKotlinxIoPath(),
                    destination = destination.toKotlinxIoPath(),
                )
            }

            else -> {
                // TODO only rename the file / folder

                if (isDirectory()) {
                    throw FileKitException("atomicMove does not support moving directories with Uri for now")
                }

                copyTo(destination)
                delete()
            }
        }
    }

public actual suspend fun PlatformFile.delete(mustExist: Boolean): Unit =
    withContext(Dispatchers.IO) {
        when (androidFile) {
            is AndroidFile.FileWrapper -> SystemFileSystem.delete(
                path = toKotlinxIoPath(),
                mustExist = mustExist
            )

            is AndroidFile.UriWrapper -> {
                val documentFile = DocumentFile.fromSingleUri(FileKit.context, androidFile.uri)
                    ?: throw FileKitException("Could not access Uri as DocumentFile")

                if (documentFile.exists()) {
                    documentFile.delete()
                } else if (mustExist) {
                    throw FileKitException("Uri does not exist")
                }
            }
        }
    }

private const val BOOKMARK_FILE_PREFIX = "<<file>>"
private const val BOOKMARK_URI_PREFIX = "<<uri>>"

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    when (androidFile) {
        is AndroidFile.FileWrapper -> {
            val data = "$BOOKMARK_FILE_PREFIX${androidFile.file.path}"
            BookmarkData(data.encodeToByteArray())
        }

        is AndroidFile.UriWrapper -> {
            val uri = androidFile.uri
            val authority = uri.authority ?: throw FileKitException("Uri authority is null")
            val documentId = DocumentsContract.getTreeDocumentId(uri)
            val treeUri = DocumentsContract.buildTreeDocumentUri(authority, documentId)

            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            FileKit.context.contentResolver.takePersistableUriPermission(treeUri, flags)
            val data = "$BOOKMARK_URI_PREFIX${androidFile.uri}"
            BookmarkData(data.encodeToByteArray())
        }
    }
}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData
): PlatformFile {
    val str = bookmarkData.bytes.decodeToString()
    return when {
        str.startsWith(BOOKMARK_FILE_PREFIX) -> {
            val filePath = str.removePrefix(BOOKMARK_FILE_PREFIX)
            PlatformFile(File(filePath))
        }

        str.startsWith(BOOKMARK_URI_PREFIX) -> {
            val uriString = str.removePrefix(BOOKMARK_URI_PREFIX)
            PlatformFile(Uri.parse(uriString))
        }

        else -> throw FileKitException("Invalid bookmark data format: $str")
    }
}

private fun getUriFileSize(uri: Uri): Long? {
    return FileKit.context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
    }
}

private fun getUriFileName(uri: Uri): String {
    return FileKit.context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
    } ?: uri.lastPathSegment ?: "" // Fallback to the Uri's last path segment
}

private fun getDocumentFile(uri: Uri): DocumentFile? {
    return DocumentFile.fromSingleUri(FileKit.context, uri)
        ?: DocumentFile.fromTreeUri(FileKit.context, uri)
}
