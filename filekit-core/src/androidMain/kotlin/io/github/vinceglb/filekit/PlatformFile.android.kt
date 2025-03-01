package io.github.vinceglb.filekit

import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitUriPathNotSupportedException
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

public actual data class PlatformFile(
    val androidFile: AndroidFile
)

public sealed class AndroidFile {
    public data class FileWrapper(val file: File) : AndroidFile()
    public data class UriWrapper(val uri: Uri) : AndroidFile()
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(File(path.toString())))

public fun PlatformFile(uri: Uri): PlatformFile =
    PlatformFile(AndroidFile.UriWrapper(uri))

public fun PlatformFile(file: File): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(file))

public actual fun PlatformFile.toPath(): Path = when (androidFile) {
    is AndroidFile.FileWrapper -> androidFile.file.toKotlinxPath()
    is AndroidFile.UriWrapper -> throw FileKitUriPathNotSupportedException()
}

public actual val PlatformFile.name: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> toPath().name
        is AndroidFile.UriWrapper -> getUriFileName(androidFile.uri)
    }

public actual val PlatformFile.extension: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.extension
        is AndroidFile.UriWrapper -> getUriFileName(androidFile.uri).substringAfterLast(".", "")
    }

public actual val PlatformFile.nameWithoutExtension: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> androidFile.file.nameWithoutExtension
        is AndroidFile.UriWrapper -> getUriFileName(androidFile.uri).substringBeforeLast(".", "")
    }

public actual fun PlatformFile.isRegularFile(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toPath())?.isRegularFile ?: false
    is AndroidFile.UriWrapper -> DocumentFile.fromSingleUri(
        FileKit.context,
        androidFile.uri
    )?.isFile == true
}

public actual fun PlatformFile.isDirectory(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toPath())?.isDirectory ?: false
    is AndroidFile.UriWrapper -> try {
        DocumentFile.fromTreeUri(
            FileKit.context,
            androidFile.uri
        )?.isDirectory == true
    } catch (e: Exception) {
        false
    }
}

public actual fun PlatformFile.exists(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.exists(toPath())
    is AndroidFile.UriWrapper -> getDocumentFile(androidFile.uri)?.exists() == true
}

public actual fun PlatformFile.size(): Long = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toPath())?.size ?: -1
    is AndroidFile.UriWrapper -> getUriFileSize(androidFile.uri) ?: -1
}

public actual fun PlatformFile.parent(): PlatformFile? = when (androidFile) {
    is AndroidFile.FileWrapper -> toPath().parent?.let(::PlatformFile)
    is AndroidFile.UriWrapper -> DocumentFile.fromSingleUri(
        FileKit.context,
        androidFile.uri
    )?.parentFile?.let { PlatformFile(it.uri) }
}

public actual fun PlatformFile.absolutePath(): PlatformFile = androidFile.let { androidFile ->
    when (androidFile) {
        is AndroidFile.FileWrapper -> PlatformFile(SystemFileSystem.resolve(toPath()))
        is AndroidFile.UriWrapper -> throw FileKitUriPathNotSupportedException()
    }
}

public actual fun PlatformFile.source(): RawSource = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.source(toPath())

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openInputStream(androidFile.uri)
        ?.asSource()
        ?: throw FileKitException("Could not open input stream for Uri")
}

public actual fun PlatformFile.sink(append: Boolean): RawSink = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.sink(toPath(), append)

    is AndroidFile.UriWrapper -> FileKit.context.contentResolver
        .openOutputStream(androidFile.uri)
        ?.asSink()
        ?: throw FileKitException("Could not open output stream for Uri")
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
