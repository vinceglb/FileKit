package io.github.vinceglb.filekit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitUriPathNotSupportedException
import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.div
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a file on the Android platform.
 *
 * This class wraps either a [File] (for filesystem paths) or a [Uri] (for content providers).
 *
 * @property androidFile The underlying [AndroidFile] wrapper (File or Uri).
 */
@Serializable(with = PlatformFileSerializer::class)
public actual data class PlatformFile(
    val androidFile: AndroidFile,
) {
    public actual override fun toString(): String = path

    public actual companion object
}

/**
 * Wrapper for Android file representations.
 */
public sealed class AndroidFile {
    /**
     * Wraps a standard Java [File].
     */
    public data class FileWrapper(
        val file: File,
    ) : AndroidFile()

    /**
     * Wraps an Android [Uri].
     */
    public data class UriWrapper(
        val uri: Uri,
    ) : AndroidFile()
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(File(path.toString())))

/**
 * Creates a [PlatformFile] from an Android [Uri].
 *
 * @param uri The [Uri] to wrap.
 * @return A [PlatformFile] instance.
 */
public fun PlatformFile(uri: Uri): PlatformFile =
    uri.toFileOrNull()?.let(::PlatformFile)
        ?: PlatformFile(AndroidFile.UriWrapper(uri))

/**
 * Creates a [PlatformFile] from a Java [File].
 *
 * @param file The [File] to wrap.
 * @return A [PlatformFile] instance.
 */
public fun PlatformFile(file: File): PlatformFile =
    PlatformFile(AndroidFile.FileWrapper(file))

public actual fun PlatformFile(path: String): PlatformFile {
    // If the path looks like an Android Uri ("content://" or "file://" scheme),
    // parse it accordingly, otherwise treat it as a regular filesystem path.
    // "file://" values are normalized to FileWrapper by PlatformFile(uri).
    return if (path.startsWith("content://", ignoreCase = true) ||
        path.startsWith("file://", ignoreCase = true)
    ) {
        @SuppressLint("UseKtx")
        PlatformFile(Uri.parse(path))
    } else {
        PlatformFile(AndroidFile.FileWrapper(File(path)))
    }
}

public actual fun PlatformFile(base: PlatformFile, child: String): PlatformFile {
    return when (val baseFile = base.androidFile) {
        is AndroidFile.FileWrapper -> {
            // For file-based paths, use kotlinx.io Path
            PlatformFile(base.toKotlinxIoPath() / child)
        }

        is AndroidFile.UriWrapper -> {
            val childUri = baseFile.uri.buildChildDocumentUri(child)
            childUri.findChildDocumentInfo()?.let { existing ->
                return PlatformFile(existing.uri)
            }

            PlatformFile(childUri)
        }
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
            else -> getUriFileName(androidFile.uri).substringBeforeLast(".")
        }
    }

public actual val PlatformFile.path: String
    get() = when (androidFile) {
        is AndroidFile.FileWrapper -> toKotlinxIoPath().toString()
        is AndroidFile.UriWrapper -> androidFile.uri.toString()
    }

public actual fun PlatformFile.isRegularFile(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> {
        SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isRegularFile
            ?: false
    }

    is AndroidFile.UriWrapper -> {
        androidFile.uri.isRegularDocument()
    }
}

public actual fun PlatformFile.isDirectory(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isDirectory
        ?: false

    is AndroidFile.UriWrapper -> androidFile.uri.isDirectoryDocument()
}

public actual fun PlatformFile.isAbsolute(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> toKotlinxIoPath().isAbsolute
    is AndroidFile.UriWrapper -> true
}

public actual fun PlatformFile.exists(): Boolean = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.exists(toKotlinxIoPath())
    is AndroidFile.UriWrapper -> androidFile.uri.existsAsDocument()
}

public actual fun PlatformFile.size(): Long = when (androidFile) {
    is AndroidFile.FileWrapper -> SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.size ?: -1
    is AndroidFile.UriWrapper -> getUriFileSize(androidFile.uri) ?: -1
}

public actual fun PlatformFile.parent(): PlatformFile? = when (androidFile) {
    is AndroidFile.FileWrapper -> {
        toKotlinxIoPath().parent?.let(::PlatformFile)
    }

    is AndroidFile.UriWrapper -> {
        val uri = androidFile.uri
        val parentUri = uri
            .buildUpon()
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

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = this.androidFile.let { androidFile ->
    when (androidFile) {
        is AndroidFile.FileWrapper -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = Files.readAttributes(
                    androidFile.file.toPath(),
                    BasicFileAttributes::class.java,
                )
                val timestamp = attributes.creationTime().toMillis()
                Instant.fromEpochMilliseconds(timestamp)
            } else {
                // Fallback for older Android versions
                null
            }
        }

        is AndroidFile.UriWrapper -> {
            null
        }
    }
}

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant {
    val timestamp = this.androidFile.let { androidFile ->
        when (androidFile) {
            is AndroidFile.FileWrapper -> {
                androidFile.file.lastModified()
            }

            is AndroidFile.UriWrapper -> {
                DocumentFile
                    .fromSingleUri(FileKit.context, androidFile.uri)
                    ?.lastModified()
                    ?: throw IllegalStateException("Unable to get last modified date for URI")
            }
        }
    }

    return Instant.fromEpochMilliseconds(timestamp)
}

public actual fun PlatformFile.mimeType(): MimeType? {
    if (isDirectory()) {
        return null
    }

    return when (androidFile) {
        is AndroidFile.FileWrapper -> {
            val mimeTypeValue = getMimeTypeValueFromExtension(extension)
            mimeTypeValue?.let(MimeType::parse)
        }

        is AndroidFile.UriWrapper -> {
            val mimeTypeValueFromContentResolver =
                FileKit.context.contentResolver.getType(androidFile.uri)
            val mimeTypeValue =
                mimeTypeValueFromContentResolver ?: getMimeTypeValueFromExtension(extension)
            mimeTypeValue?.let(MimeType::parse)
        }
    }
}

private fun getMimeTypeValueFromExtension(extension: String): String? {
    val safeExtension = extension.trim().lowercase()

    if (safeExtension.isEmpty()) return null

    return MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(safeExtension)
        ?.trim()
        ?.lowercase()
}

private const val DEFAULT_STREAM_MIME_TYPE = "application/octet-stream"

internal actual suspend fun PlatformFile.prepareDestinationForWrite(
    source: PlatformFile,
): PlatformFile = withContext(Dispatchers.IO) {
    if (!isDirectory()) {
        return@withContext this@prepareDestinationForWrite
    }

    when (val target = androidFile) {
        is AndroidFile.FileWrapper -> {
            val path = toKotlinxIoPath() / source.name
            PlatformFile(path)
        }

        is AndroidFile.UriWrapper -> {
            val context = FileKit.context

            val directoryDocument = DocumentFile.fromTreeUri(context, target.uri)
                ?: DocumentFile.fromSingleUri(context, target.uri)
                ?: throw FileKitException("Could not access Uri as DocumentFile")

            directoryDocument.findFile(source.name)?.let { existing ->
                if (existing.isDirectory) {
                    throw FileKitException("Destination already contains a directory named ${source.name}")
                }

                return@withContext PlatformFile(existing.uri)
            }

            val mimeType = resolveMimeTypeForCopy(source)
            val created = directoryDocument.createFile(mimeType, source.name)
                ?: throw FileKitException("Could not create destination file in bookmarked directory")

            PlatformFile(created.uri)
        }
    }
}

private fun resolveMimeTypeForCopy(source: PlatformFile): String = source.mimeType()?.toString()
    ?: getMimeTypeValueFromExtension(source.extension)
    ?: DEFAULT_STREAM_MIME_TYPE

public actual fun PlatformFile.source(): RawSource = when (androidFile) {
    is AndroidFile.FileWrapper -> {
        SystemFileSystem.source(toKotlinxIoPath())
    }

    is AndroidFile.UriWrapper -> {
        FileKit.context.contentResolver
            .openInputStream(androidFile.uri)
            ?.asSource()
            ?: throw FileKitException("Could not open input stream for Uri")
    }
}

public actual fun PlatformFile.sink(append: Boolean): RawSink = when (androidFile) {
    is AndroidFile.FileWrapper -> {
        SystemFileSystem.sink(toKotlinxIoPath(), append)
    }

    is AndroidFile.UriWrapper -> {
        val uri = androidFile.uri.ensureFileDocument()
        // Use "wt" (write+truncate) for overwrite, "wa" (write+append) for append
        // This ensures existing file content is properly truncated when overwriting
        val mode = if (append) "wa" else "wt"
        val fos = FileKit.context.contentResolver
            .openFileDescriptor(uri, mode)
            ?.let { ParcelFileDescriptor.AutoCloseOutputStream(it) }
            ?: throw FileKitException("Could not open output stream for Uri")

        // If overwriting, explicitly set the size to 0
        // This is necessary because Truncate does not work well for Google Drive Uri.
        if (!append) {
            try {
                fos.channel.truncate(0)
            } catch (e: Exception) {
                fos.close()
                throw e
            }
        }

        fos.asSink()
    }
}

public actual fun PlatformFile.createDirectories(mustCreate: Boolean): Unit = when (val file = androidFile) {
    is AndroidFile.FileWrapper -> {
        SystemFileSystem.createDirectories(toKotlinxIoPath(), mustCreate)
    }

    is AndroidFile.UriWrapper -> {
        file.uri.ensureDirectoryDocument(mustCreate)
    }
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
            block(androidFile.uri.listDocuments())
        }
    }
}

public actual fun PlatformFile.list(): List<PlatformFile> = when (androidFile) {
    is AndroidFile.FileWrapper -> {
        SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
    }

    is AndroidFile.UriWrapper -> {
        androidFile.uri.listDocuments()
    }
}

public actual suspend fun PlatformFile.atomicMove(destination: PlatformFile): Unit =
    withContext(Dispatchers.IO) {
        when {
            androidFile is AndroidFile.FileWrapper && destination.androidFile is AndroidFile.FileWrapper -> {
                val resolvedDestination = destination.resolveAtomicMoveDestination(
                    source = this@atomicMove,
                )
                SystemFileSystem.atomicMove(
                    source = toKotlinxIoPath(),
                    destination = resolvedDestination.toKotlinxIoPath(),
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

private fun PlatformFile.resolveAtomicMoveDestination(source: PlatformFile): PlatformFile {
    if (androidFile is AndroidFile.FileWrapper && isDirectory()) {
        return PlatformFile(toKotlinxIoPath() / source.name)
    }
    return this
}

public actual suspend fun PlatformFile.delete(mustExist: Boolean): Unit =
    withContext(Dispatchers.IO) {
        when (androidFile) {
            is AndroidFile.FileWrapper -> {
                SystemFileSystem.delete(
                    path = toKotlinxIoPath(),
                    mustExist = mustExist,
                )
            }

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

@Deprecated("Please do not use this anymore. Keep it for backward compatibility only.")
private const val BOOKMARK_FILE_PREFIX = "<<file>>"

@Deprecated("Please do not use this anymore. Keep it for backward compatibility only.")
private const val BOOKMARK_URI_PREFIX = "<<uri>>"

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    when (androidFile) {
        is AndroidFile.FileWrapper -> {
            val data = androidFile.file.path
            BookmarkData(data.encodeToByteArray())
        }

        is AndroidFile.UriWrapper -> {
            val uri = androidFile.uri

            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            // Check if this is a tree URI (directory) or document URI (file)
            val uriToPermission = if (isDirectory()) {
                // For directories, we need to get the tree URI
                val documentId = DocumentsContract.getTreeDocumentId(uri)
                val authority = uri.authority ?: throw FileKitException("Uri authority is null")
                DocumentsContract.buildTreeDocumentUri(authority, documentId)
            } else {
                // For files, use the URI directly
                uri
            }

            FileKit.context.contentResolver.takePersistableUriPermission(uriToPermission, flags)
            val data = androidFile.uri.toString()
            BookmarkData(data.encodeToByteArray())
        }
    }
}

public actual fun PlatformFile.releaseBookmark() {
    when (androidFile) {
        is AndroidFile.FileWrapper -> {}

        // No action needed for regular files
        is AndroidFile.UriWrapper -> {
            val uriToRelease = androidFile.uri.getUriToRelease(isDirectory())
            val flags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            FileKit.context.contentResolver.releasePersistableUriPermission(uriToRelease, flags)
        }
    }
}

private fun Uri.getUriToRelease(isDirectory: Boolean): Uri = if (isDirectory) {
    val authority = this.authority ?: throw FileKitException("Uri authority is null")
    val documentId = DocumentsContract.getTreeDocumentId(this)
    DocumentsContract.buildTreeDocumentUri(authority, documentId)
} else {
    this
}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile {
    val str = bookmarkData.bytes.decodeToString()

    val platformFile = when {
        // Content Uri always starts with "content://"
        str.startsWith("content://") -> {
            val uriString = str
            @SuppressLint("UseKtx")
            PlatformFile(Uri.parse(uriString))
        }

        // Very rarely used Uri, discouraged and deprecated, may starts with "file://"
        // Parse through PlatformFile(Uri) so encoded paths (e.g. "%20") resolve correctly.
        str.startsWith("file://") -> {
            val uriString = str
            @SuppressLint("UseKtx")
            PlatformFile(Uri.parse(uriString))
        }

        // TODO remove this in future
        str.startsWith(BOOKMARK_URI_PREFIX) -> {
            val uriString = str.removePrefix(BOOKMARK_URI_PREFIX)
            @SuppressLint("UseKtx")
            PlatformFile(Uri.parse(uriString))
        }

        str.startsWith(BOOKMARK_FILE_PREFIX) -> {
            val filePath = str.removePrefix(BOOKMARK_FILE_PREFIX)
            PlatformFile(File(filePath))
        }

        // Removes a chance of exception throwing
        else -> {
            val filePath = str
            PlatformFile(File(filePath))
        }
    }

    if (!platformFile.exists()) {
        throw FileKitException("Bookmark target is no longer accessible")
    }

    return platformFile
}

private fun getUriFileSize(uri: Uri): Long? = UriMetadataResolver.size(uri)

private fun getUriFileName(uri: Uri): String = UriMetadataResolver.displayName(uri)

private object UriMetadataResolver {
    fun displayName(uri: Uri): String = resolve(uri).displayName ?: fallbackDisplayName(uri)

    fun size(uri: Uri): Long? = resolve(uri).size

    private fun resolve(uri: Uri): UriMetadata {
        val queryUri = uri.toDocumentUriForMetadata()
        val openableMetadata = queryOpenableMetadata(queryUri) ?: UriMetadata.empty
        val resolvedDisplayName = chooseDisplayName(
            uri = uri,
            openableDisplayName = openableMetadata.displayName,
        )
        return openableMetadata.copy(displayName = resolvedDisplayName)
    }

    private fun queryOpenableMetadata(queryUri: Uri): UriMetadata? = try {
        FileKit.context.contentResolver
            .query(
                queryUri,
                OPENABLE_METADATA_PROJECTION,
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) {
                    return null
                }

                val displayName = cursor
                    .readNullableString(OpenableColumns.DISPLAY_NAME)
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                val size = cursor.readNullableLong(OpenableColumns.SIZE)
                UriMetadata(
                    displayName = displayName,
                    size = size,
                )
            }
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun chooseDisplayName(
        uri: Uri,
        openableDisplayName: String?,
    ): String? {
        if (!uri.isPhotoPickerUri()) {
            return openableDisplayName
        }

        if (openableDisplayName != null && !isSyntheticPhotoPickerDisplayName(openableDisplayName)) {
            return openableDisplayName
        }

        return queryPhotoPickerMediaStoreDisplayName(uri)
            ?: fallbackDisplayName(
                uri = uri,
                extension = openableDisplayName
                    ?.takeIf(::isSyntheticPhotoPickerDisplayName)
                    ?.substringAfterLast('.', "")
                    ?.takeIf(String::isNotEmpty),
            )
    }

    private fun queryPhotoPickerMediaStoreDisplayName(uri: Uri): String? {
        val mediaId = uri.extractPhotoPickerMediaId() ?: return null
        val selection = "${MediaStore.MediaColumns._ID}=?"
        val selectionArgs = arrayOf(mediaId.toString())
        val resolver = FileKit.context.contentResolver

        for (collectionUri in mediaStoreCollectionsForPhotoPicker()) {
            val displayName = try {
                resolver
                    .query(
                        collectionUri,
                        MEDIASTORE_DISPLAY_NAME_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                    )?.use { cursor ->
                        if (!cursor.moveToFirst()) {
                            null
                        } else {
                            cursor
                                .readNullableString(MediaStore.MediaColumns.DISPLAY_NAME)
                                ?.trim()
                                ?.takeIf(String::isNotEmpty)
                        }
                    }
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }

            if (displayName != null) {
                return displayName
            }
        }

        return null
    }

    private fun mediaStoreCollectionsForPhotoPicker(): List<Uri> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        ).distinctBy(Uri::toString)
    } else {
        listOf(
            MediaStore.Files.getContentUri("external"),
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        )
    }

    private fun fallbackDisplayName(
        uri: Uri,
        extension: String? = null,
    ): String = when {
        uri.isPhotoPickerUri() -> {
            val providerName = uri.photoPickerProviderName()
            val uniqueSuffix = uri.extractPhotoPickerMediaId()?.toString()
                ?: uri.lastPathSegment

            val baseName = when {
                providerName != null && uniqueSuffix != null -> "$providerName-$uniqueSuffix"
                providerName != null -> providerName
                uniqueSuffix != null -> uniqueSuffix
                else -> ""
            }

            baseName.withExtension(extension)
        }

        else -> {
            uri.lastPathSegment ?: ""
        }
    }

    private fun isSyntheticPhotoPickerDisplayName(displayName: String): Boolean =
        SYNTHETIC_PHOTO_PICKER_NAME_REGEX.matches(displayName)

    private fun String.withExtension(extension: String?): String {
        if (isEmpty() || extension.isNullOrBlank()) {
            return this
        }

        return "$this.$extension"
    }

    private fun Uri.extractPhotoPickerMediaId(): Long? {
        lastPathSegment?.toLongOrNull()?.let { return it }

        val documentId = runCatching { DocumentsContract.getDocumentId(this) }.getOrNull()
            ?: runCatching { DocumentsContract.getTreeDocumentId(this) }.getOrNull()
            ?: return null

        return documentId.substringAfterLast(':').toLongOrNull()
    }

    private fun Uri.isPhotoPickerUri(): Boolean {
        if (!scheme.equals("content", ignoreCase = true)) {
            return false
        }

        val segments = pathSegments
        val hasPickerPath = segments.contains("picker") || segments.contains("photopicker")
        return when (authority) {
            PHOTO_PICKER_MEDIA_AUTHORITY -> hasPickerPath
            PHOTO_PICKER_PROVIDER_AUTHORITY -> true
            else -> false
        }
    }

    private fun Uri.photoPickerProviderName(): String? {
        val segments = pathSegments
        val pickerIndex = segments.indexOf("picker")
        val providerSegment = if (pickerIndex == -1) {
            null
        } else {
            segments.getOrNull(pickerIndex + 2)
        }

        return providerSegment
            ?.substringAfterLast('.')
            ?.takeIf(String::isNotBlank)
            ?: authority
                ?.substringAfterLast('.')
                ?.takeIf(String::isNotBlank)
    }

    private fun android.database.Cursor.readNullableString(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index == -1 || isNull(index)) {
            null
        } else {
            getString(index)
        }
    }

    private fun android.database.Cursor.readNullableLong(columnName: String): Long? {
        val index = getColumnIndex(columnName)
        return if (index == -1 || isNull(index)) {
            null
        } else {
            getLong(index)
        }
    }

    private data class UriMetadata(
        val displayName: String?,
        val size: Long?,
    ) {
        companion object {
            val empty = UriMetadata(displayName = null, size = null)
        }
    }

    private val OPENABLE_METADATA_PROJECTION = arrayOf(
        OpenableColumns.DISPLAY_NAME,
        OpenableColumns.SIZE,
    )
    private val MEDIASTORE_DISPLAY_NAME_PROJECTION = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    private val SYNTHETIC_PHOTO_PICKER_NAME_REGEX = Regex("^\\d+(?:\\.[A-Za-z0-9]+)?$")
    private const val PHOTO_PICKER_MEDIA_AUTHORITY = "media"
    private const val PHOTO_PICKER_PROVIDER_AUTHORITY = "com.android.providers.media.photopicker"
}

private fun Uri.toDocumentUriForMetadata(): Uri {
    if (!isTreeUriCompat()) return this

    val documentId = try {
        DocumentsContract.getDocumentId(this)
    } catch (_: IllegalArgumentException) {
        DocumentsContract.getTreeDocumentId(this)
    }

    return DocumentsContract.buildDocumentUriUsingTree(this, documentId)
}

private fun Uri.ensureDirectoryDocument(mustCreate: Boolean) {
    findChildDocumentInfo()?.let { document ->
        if (!document.isDirectory) {
            throw FileKitException("Uri exists but is not a directory: $this")
        }
        if (mustCreate) {
            throw FileKitException("Directory already exists: $this")
        }
        return
    }

    if (!isChildDocumentUri()) {
        getDocumentFile(this)?.takeIf { it.exists() }?.let { documentFile ->
            if (!documentFile.isDirectory) {
                throw FileKitException("Uri exists but is not a directory: $this")
            }
            if (mustCreate) {
                throw FileKitException("Directory already exists: $this")
            }
            return
        }
    }

    ensureDirectoryPath(mustCreate)
}

private fun Uri.ensureFileDocument(): Uri {
    findChildDocumentInfo()?.let { document ->
        if (document.isDirectory) {
            throw FileKitException("Uri exists but is a directory: $this")
        }
        return document.uri
    }

    if (!isChildDocumentUri()) {
        getDocumentFile(this)?.takeIf { it.exists() }?.let { documentFile ->
            if (documentFile.isDirectory) {
                throw FileKitException("Uri exists but is a directory: $this")
            }
            return documentFile.uri
        }
    }

    val (parentUri, childName) = parentDocumentUriAndName()
    val extension = childName.substringAfterLast('.', "")
    val mimeType = getMimeTypeValueFromExtension(extension) ?: DEFAULT_STREAM_MIME_TYPE

    return DocumentsContract.createDocument(
        FileKit.context.contentResolver,
        parentUri,
        mimeType,
        childName,
    )
        ?: throw FileKitException("Could not create file: $childName")
}

private fun Uri.buildChildDocumentUri(child: String): Uri {
    val parentDocumentId = documentId()
    val childDocumentId = listOf(parentDocumentId, child)
        .filter(String::isNotEmpty)
        .joinToString("/")

    return DocumentsContract.buildDocumentUriUsingTree(this, childDocumentId)
}

private fun Uri.existsAsDocument(): Boolean {
    findChildDocumentInfo()?.let { return true }
    if (isChildDocumentUri()) return false
    return getDocumentFile(this)?.exists() == true
}

private fun Uri.isDirectoryDocument(): Boolean {
    findChildDocumentInfo()?.let { return it.isDirectory }
    if (isChildDocumentUri()) return false
    return getDocumentFile(this)?.isDirectory == true
}

private fun Uri.isRegularDocument(): Boolean {
    findChildDocumentInfo()?.let { return !it.isDirectory }
    if (isChildDocumentUri()) return false
    return getDocumentFile(this)?.isFile == true
}

@PublishedApi
internal fun Uri.listDocuments(): List<PlatformFile> {
    if (!isDirectoryDocument()) {
        throw FileKitException("Uri is not a directory: $this")
    }

    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(this, documentId())
    return queryDocumentInfos(childrenUri).map { PlatformFile(it.uri) }
}

private fun Uri.findChildDocumentInfo(): AndroidDocumentInfo? {
    val (parentDocumentId, childName) = parentDocumentIdAndNameOrNull() ?: return null

    return findChildDocumentInfo(
        parentDocumentId = parentDocumentId,
        childName = childName,
    )
}

private fun Uri.findChildDocumentInfo(
    parentDocumentId: String,
    childName: String,
): AndroidDocumentInfo? {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(this, parentDocumentId)

    return queryDocumentInfos(childrenUri).firstOrNull { it.name == childName }
}

private fun Uri.ensureDirectoryPath(mustCreate: Boolean) {
    val treeDocumentId = treeDocumentId()
    val targetDocumentId = documentId()

    if (targetDocumentId == treeDocumentId) {
        throw FileKitException("Directory is not accessible: $this")
    }
    if (!targetDocumentId.startsWith("$treeDocumentId/")) {
        throw FileKitException("Uri is outside the granted tree: $this")
    }

    val relativeSegments = targetDocumentId
        .removePrefix("$treeDocumentId/")
        .split("/")
        .filter(String::isNotEmpty)
    var parentDocumentId = treeDocumentId
    var parentUri = DocumentsContract.buildDocumentUriUsingTree(this, parentDocumentId)

    relativeSegments.forEachIndexed { index, segment ->
        val isTargetDirectory = index == relativeSegments.lastIndex
        val existing = findChildDocumentInfo(
            parentDocumentId = parentDocumentId,
            childName = segment,
        )

        if (existing != null) {
            if (!existing.isDirectory) {
                throw FileKitException("Uri exists but is not a directory: ${existing.uri}")
            }
            if (isTargetDirectory && mustCreate) {
                throw FileKitException("Directory already exists: ${existing.uri}")
            }
            parentDocumentId = existing.documentId
            parentUri = existing.uri
            return@forEachIndexed
        }

        val createdUri = DocumentsContract.createDocument(
            FileKit.context.contentResolver,
            parentUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            segment,
        ) ?: throw FileKitException("Could not create directory: $segment")

        parentDocumentId = createdUri.documentId()
        parentUri = createdUri
    }
}

private fun Uri.queryDocumentInfos(childrenUri: Uri): List<AndroidDocumentInfo> = try {
    FileKit.context.contentResolver
        .query(
            childrenUri,
            ANDROID_DOCUMENT_INFO_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            val documentIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val documents = mutableListOf<AndroidDocumentInfo>()

            while (cursor.moveToNext()) {
                val displayName = if (nameIndex == -1 || cursor.isNull(nameIndex)) {
                    null
                } else {
                    cursor.getString(nameIndex)
                }

                val documentId = if (documentIdIndex == -1 || cursor.isNull(documentIdIndex)) {
                    continue
                } else {
                    cursor.getString(documentIdIndex)
                }
                val mimeType = if (mimeTypeIndex == -1 || cursor.isNull(mimeTypeIndex)) {
                    null
                } else {
                    cursor.getString(mimeTypeIndex)
                }

                documents += AndroidDocumentInfo(
                    uri = DocumentsContract.buildDocumentUriUsingTree(
                        this,
                        documentId,
                    ),
                    documentId = documentId,
                    name = displayName,
                    mimeType = mimeType,
                )
            }

            documents
        } ?: emptyList()
} catch (_: SecurityException) {
    emptyList()
} catch (_: IllegalArgumentException) {
    emptyList()
}

private fun Uri.parentDocumentUriAndName(): Pair<Uri, String> {
    val (parentDocumentId, childName) = parentDocumentIdAndNameOrNull()
        ?: throw FileKitException("Uri does not describe a child document: $this")

    return DocumentsContract.buildDocumentUriUsingTree(this, parentDocumentId) to childName
}

private fun Uri.parentDocumentIdAndNameOrNull(): Pair<String, String>? {
    val documentId = try {
        documentId()
    } catch (_: IllegalArgumentException) {
        return null
    }
    val parentDocumentId = documentId.substringBeforeLast('/', missingDelimiterValue = "")
    val childName = documentId.substringAfterLast('/')

    if (parentDocumentId.isEmpty() || childName.isEmpty() || childName == documentId) {
        return null
    }

    return parentDocumentId to childName
}

private fun Uri.isChildDocumentUri(): Boolean {
    val segments = pathSegments
    return "tree" in segments && "document" in segments && parentDocumentIdAndNameOrNull() != null
}

private fun Uri.documentId(): String = try {
    DocumentsContract.getDocumentId(this)
} catch (_: IllegalArgumentException) {
    DocumentsContract.getTreeDocumentId(this)
}

private fun Uri.treeDocumentId(): String =
    DocumentsContract.getTreeDocumentId(this)

private fun Uri.isTreeUriCompat(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return DocumentsContract.isTreeUri(this)
    }

    val segments = pathSegments ?: return false
    return segments.isNotEmpty() && segments[0] == "tree"
}

private fun getDocumentFile(uri: Uri): DocumentFile? {
    val context = FileKit.context
    return when {
        DocumentsContract.isDocumentUri(context, uri) -> {
            DocumentFile.fromSingleUri(context, uri)
        }

        uri.isTreeUriCompat() -> {
            DocumentFile.fromTreeUri(context, uri)
        }

        else -> {
            DocumentFile.fromSingleUri(context, uri)
                ?: DocumentFile.fromTreeUri(context, uri)
        }
    }
}

private data class AndroidDocumentInfo(
    val uri: Uri,
    val documentId: String,
    val name: String?,
    val mimeType: String?,
) {
    val isDirectory: Boolean = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
}

private val ANDROID_DOCUMENT_INFO_PROJECTION = arrayOf(
    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
    DocumentsContract.Document.COLUMN_MIME_TYPE,
)

private fun Uri.toFileOrNull(): File? {
    if (!scheme.equals("file", ignoreCase = true)) {
        return null
    }

    val filePath = path ?: return null
    return File(filePath)
}
