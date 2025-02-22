package io.github.vinceglb.filekit

import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

// Android implementation of PlatformFile, wrapping either a File or a Uri
public actual data class PlatformFile(
    val androidFile: AndroidFile
)

// Sealed class for wrapping Android-specific file types
public sealed class AndroidFile {
    public data class FileWrapper(val file: File) : AndroidFile()
    public data class UriWrapper(val uri: Uri) : AndroidFile()
}

// Constructors

// Create a PlatformFile from a Path
public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(File(path.toString())))

// Create a PlatformFile from a Uri
public fun PlatformFile(uri: Uri): PlatformFile =
    PlatformFile(AndroidFile.UriWrapper(uri))

// Create a PlatformFile from a File
public fun PlatformFile(file: File): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(file))

// Extension Properties

// Get the Path representation, only for File-based PlatformFile
public actual val PlatformFile.path: Path?
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.toKotlinxPath()
        is AndroidFile.UriWrapper -> null // Uri does not directly map to a Path
    }

// Check if PlatformFile is a file (only works for File-based PlatformFile)
public actual val PlatformFile.isFile: Boolean
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.isFile
        is AndroidFile.UriWrapper -> DocumentFile.fromSingleUri(
            FileKit.context,
            androidFile.uri
        )?.isFile == true
    }

// Check if PlatformFile is a directory (only works for File-based PlatformFile)
public actual val PlatformFile.isDirectory: Boolean
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.isDirectory
        is AndroidFile.UriWrapper -> try {
            DocumentFile.fromTreeUri(
                FileKit.context,
                androidFile.uri
            )?.isDirectory == true
        } catch (e: Exception) {
            false
        }
    }

// Check if PlatformFile exists
public actual val PlatformFile.exists: Boolean
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.exists()
        is AndroidFile.UriWrapper -> getDocumentFile(androidFile.uri)?.exists() == true
    }

// Get the size of the file in bytes. For Uri, it will query the content resolver.
public actual val PlatformFile.size: Long?
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.length()
        is AndroidFile.UriWrapper -> getUriFileSize(androidFile.uri)
    }

// Get the name of the file. For Uri, it queries the content resolver.
public actual val PlatformFile.name: String?
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.name
        is AndroidFile.UriWrapper -> getUriFileName(androidFile.uri)
    }

// Get the parent directory path, only applicable for File-based PlatformFile
public actual val PlatformFile.parent: PlatformFile?
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.parentFile?.let { PlatformFile(it.toKotlinxPath()) }
        is AndroidFile.UriWrapper -> DocumentFile.fromSingleUri(
            FileKit.context,
            androidFile.uri
        )?.parentFile?.let { PlatformFile(it.uri) }
    }

public actual val PlatformFile.absolutePath: String
    get() = androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.UriWrapper -> androidFile.uri.path ?: ""
            is AndroidFile.FileWrapper -> androidFile.file.absolutePath
        }
    }

// IO Operations with kotlinx-io

// Get Source for reading from PlatformFile
public actual fun PlatformFile.source(): RawSource? = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem
        .source(androidFile.file.toKotlinxPath())

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openInputStream(androidFile.uri)
        ?.asSource()
}

// Get Sink for writing to PlatformFile, append option only for File
public actual fun PlatformFile.sink(append: Boolean): RawSink? = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem
        .sink(androidFile.file.toKotlinxPath(), append = append)

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openOutputStream(androidFile.uri)
        ?.asSink()
}

// Helper Functions for Uri-based PlatformFile

// Get the size of a Uri-based file
private fun getUriFileSize(uri: Uri): Long? {
    return FileKit.context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) cursor.getLong(sizeIndex) else null
    }
}

// Get the name of a Uri-based file
private fun getUriFileName(uri: Uri): String? {
    return FileKit.context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst()) cursor.getString(nameIndex) else null
    } ?: uri.lastPathSegment // Fallback to the Uri's last path segment
}

// Get the DocumentFile from a Uri
private fun getDocumentFile(uri: Uri): DocumentFile? {
    return DocumentFile.fromSingleUri(FileKit.context, uri)
        ?: DocumentFile.fromTreeUri(FileKit.context, uri)
}
