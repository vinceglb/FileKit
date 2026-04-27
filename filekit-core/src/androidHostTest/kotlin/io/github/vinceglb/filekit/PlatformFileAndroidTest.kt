@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.exceptions.FileKitUriPathNotSupportedException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformFileAndroidTest {
    private var retainedContext: Context? = null

    @Before
    fun setup() {
        ShadowContentResolver.reset()
        // Initialize FileKit with Robolectric's application context
        retainedContext = RuntimeEnvironment.getApplication()
        FileKit.manualFileKitCoreInitialization(requireNotNull(retainedContext))
    }

    private fun initializeFileKitWithResolver(resolver: ContentResolver) {
        val appContext = RuntimeEnvironment.getApplication() as Context
        val wrappedContext = object : ContextWrapper(appContext) {
            override fun getContentResolver(): ContentResolver = resolver
        }
        retainedContext = wrappedContext
        FileKit.manualFileKitCoreInitialization(requireNotNull(retainedContext))
    }

    private val resourceDirectory = FileKit.projectDir / "src/nonWebTest/resources"
    private val textFile = resourceDirectory / "hello.txt"
    private val imageFile = resourceDirectory / "compose-logo.png"
    private val emptyFile = resourceDirectory / "empty-file"
    private val notExistingFile = resourceDirectory / "not-existing-file.pdf"

    private fun createTempAudioFileWithEncodedSpacePath(): Pair<File, String> {
        val directory = File.createTempFile("filekit-file-uri-", "").apply {
            delete()
            mkdirs()
            deleteOnExit()
        }
        val file = File(directory, "encoded space audio.mp3").apply {
            writeText("audio")
            deleteOnExit()
        }
        val encodedFileUri = "file://${file.absolutePath.replace(" ", "%20")}"
        return file to encodedFileUri
    }

    @Test
    fun testPlatformMimeType() {
        assertEquals(
            expected = MimeType.parse("text/plain"),
            actual = textFile.mimeType(),
        )
        assertEquals(
            expected = MimeType.parse("image/png"),
            actual = imageFile.mimeType(),
        )
        assertEquals(
            expected = null,
            actual = emptyFile.mimeType(),
        )
        assertEquals(
            expected = MimeType.parse("application/pdf"),
            actual = notExistingFile.mimeType(),
        )
        assertEquals(
            expected = null,
            actual = resourceDirectory.mimeType(),
        )
    }

    // Issue #415: Test `/` operator on FileWrapper (should work as before)
    @Test
    fun testDivOperatorOnFileWrapper() {
        val base = PlatformFile("/tmp/test")
        val child = base / "child.txt"

        assertIs<AndroidFile.FileWrapper>(child.androidFile)
        assertEquals("/tmp/test/child.txt", child.path)
    }

    // Issue #415: Test `/` operator on UriWrapper does NOT throw FileKitUriPathNotSupportedException.
    // Full integration testing requires a real Android device with SAF support.
    @Test
    fun testDivOperatorOnUriWrapper_noLongerThrowsPathNotSupportedException() {
        // Create a Uri-based PlatformFile (tree Uri format used by directory pickers)
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        val base = PlatformFile(uri)

        val child = base / "backup.zip"

        assertIs<AndroidFile.UriWrapper>(child.androidFile)
        assertEquals(
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments/document/primary%3ADocuments%2Fbackup.zip",
            child.path,
        )
    }

    @Test
    fun createDirectories_onUriChild_doesNotUsePathRepresentation() {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        val base = PlatformFile(uri)

        val directory = base / "Notes"
        assertFalse(directory.exists())
        assertFalse(directory.isDirectory())

        val exception = runCatching {
            directory.createDirectories()
        }.exceptionOrNull()

        assertIsNot<FileKitUriPathNotSupportedException>(exception)
    }

    @Test
    fun div_onNestedUriChild_keepsUsingGrantedTreeUri() {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        val base = PlatformFile(uri)

        val nestedFile = base / "Notes" / "note.txt"

        assertIs<AndroidFile.UriWrapper>(nestedFile.androidFile)
        assertEquals(
            "content://com.android.externalstorage.documents/tree/primary%3ADocuments/document/primary%3ADocuments%2FNotes%2Fnote.txt",
            nestedFile.path,
        )
    }

    @Test
    fun list_onUriChildDirectory_returnsChildDirectoryContents() {
        val treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        ShadowContentResolver.registerProviderInternal(
            "com.android.externalstorage.documents",
            createNestedTreeContentProvider(),
        )
        val base = PlatformFile(treeUri)

        val notes = base / "Notes"

        assertEquals(listOf("Notes", "parent-only.txt"), base.list().map { it.name })
        assertEquals(listOf("note.txt"), notes.list().map { it.name })
    }

    @Test
    fun createDirectories_onNestedUriChild_createsMissingParents() {
        val treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        ShadowContentResolver.registerProviderInternal(
            "com.android.externalstorage.documents",
            createNestedTreeContentProvider(),
        )
        val base = PlatformFile(treeUri)

        val target = base / "Created" / "Nested"
        target.createDirectories()

        assertTrue((base / "Created").isDirectory())
        assertTrue(target.isDirectory())
        assertEquals(listOf("Nested"), (base / "Created").list().map { it.name })
    }

    @Test
    fun PlatformFile_fromFileSchemeString_existsAndUsesFileWrapper() {
        val (backingFile, encodedFileUri) = createTempAudioFileWithEncodedSpacePath()

        try {
            val file = PlatformFile(encodedFileUri)
            assertTrue(file.exists())
            assertIs<AndroidFile.FileWrapper>(file.androidFile)
            assertEquals(backingFile.absolutePath, file.path)
        } finally {
            backingFile.delete()
            backingFile.parentFile?.delete()
        }
    }

    @Test
    fun PlatformFile_fromFileSchemeUri_existsAndUsesFileWrapper() {
        val (backingFile, encodedFileUri) = createTempAudioFileWithEncodedSpacePath()

        try {
            val file = PlatformFile(Uri.parse(encodedFileUri))
            assertTrue(file.exists())
            assertIs<AndroidFile.FileWrapper>(file.androidFile)
            assertEquals(backingFile.absolutePath, file.path)
        } finally {
            backingFile.delete()
            backingFile.parentFile?.delete()
        }
    }

    @Test
    fun PlatformFile_fromBookmarkData_fileSchemeUriWithEncodedPath_restoresAccessibleFile() {
        val (backingFile, encodedFileUri) = createTempAudioFileWithEncodedSpacePath()

        try {
            val restored = PlatformFile.fromBookmarkData(BookmarkData(encodedFileUri.encodeToByteArray()))
            assertTrue(restored.exists())
            assertIs<AndroidFile.FileWrapper>(restored.androidFile)
            assertEquals(backingFile.absolutePath, restored.path)
        } finally {
            backingFile.delete()
            backingFile.parentFile?.delete()
        }
    }

    @Test
    fun saveVideoToGallery_sourceStreamCannotBeOpened_returnsFailureResult() {
        runBlocking {
            val file = PlatformFile(Uri.parse("content://invalid.provider/missing-video.mp4"))
            val result = FileKit.saveVideoToGallery(file = file, filename = "video.mp4")
            assertTrue(result.isFailure)
            assertIs<FileKitException>(result.exceptionOrNull())
        }
    }

    @Test
    fun saveImageToGallery_whenMediaStoreInsertFails_returnsFailureResult() {
        runBlocking {
            val resolver = ContentResolver.wrap(NullInsertContentProvider())
            initializeFileKitWithResolver(resolver)
            val result = FileKit.saveImageToGallery(bytes = byteArrayOf(1, 2, 3), filename = "image.jpg")
            assertTrue(result.isFailure)
            assertIs<FileKitException>(result.exceptionOrNull())
        }
    }

    @Test
    fun PlatformFile_size_uriWithoutSizeColumn_returnsMinusOne() {
        runBlocking {
            val sourceBytes = "filekit-copy-source".encodeToByteArray()
            val sourceUri = Uri.parse("content://filekit.test/source.bin")
            val destinationUri = Uri.parse("content://filekit.test/destination.bin")
            val provider = MissingSizeContentProvider(
                sourceUri = sourceUri,
                destinationUri = destinationUri,
                sourceBytes = sourceBytes,
            )
            initializeFileKitWithResolver(ContentResolver.wrap(provider))

            val source = PlatformFile(sourceUri)
            assertEquals(expected = -1L, actual = source.size())
        }
    }

    @Test
    fun PlatformFile_copyTo_uriWithoutSizeColumn_copiesContent() {
        runBlocking {
            val sourceBytes = "filekit-copy-source".encodeToByteArray()
            val sourceUri = Uri.parse("content://filekit.test/source.bin")
            val destinationUri = Uri.parse("content://filekit.test/destination.bin")
            val provider = MissingSizeContentProvider(
                sourceUri = sourceUri,
                destinationUri = destinationUri,
                sourceBytes = sourceBytes,
            )
            val resolver = ContentResolver.wrap(provider)
            initializeFileKitWithResolver(resolver)

            shadowOf(resolver).registerInputStream(sourceUri, ByteArrayInputStream(sourceBytes))

            val source = PlatformFile(sourceUri)
            val destinationFile = File.createTempFile("filekit-copy-destination-", ".bin")
            val destination = PlatformFile(destinationFile)
            try {
                source.copyTo(destination)
                assertContentEquals(expected = sourceBytes, actual = destination.readBytes())
            } finally {
                destination.delete(mustExist = false)
            }
        }
    }

    @Test
    fun PlatformFile_atomicMove_fileDestinationDirectory_movesIntoDirectory() {
        runBlocking {
            val sourceDirectory = File.createTempFile("filekit-atomic-source-", "").apply {
                delete()
                mkdirs()
                deleteOnExit()
            }
            val destinationDirectory = File.createTempFile("filekit-atomic-destination-", "").apply {
                delete()
                mkdirs()
                deleteOnExit()
            }
            val sourceFile = File(sourceDirectory, "source.txt").apply {
                writeText("filekit-atomic-move")
                deleteOnExit()
            }
            val movedFile = File(destinationDirectory, sourceFile.name).apply {
                deleteOnExit()
            }

            try {
                PlatformFile(sourceFile).atomicMove(PlatformFile(destinationDirectory))

                assertFalse(sourceFile.exists())
                assertTrue(movedFile.exists())
                assertEquals(expected = "filekit-atomic-move", actual = movedFile.readText())
            } finally {
                movedFile.delete()
                sourceFile.delete()
                destinationDirectory.delete()
                sourceDirectory.delete()
            }
        }
    }

    @Test
    fun PlatformFile_name_photoPickerNumericName_resolvesFromMediaStore() {
        val pickerUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/18")
        val provider = PhotoPickerNameContentProvider(
            pickerUri = pickerUri,
            pickerDisplayName = "18.jpg",
            mediaStoreDisplayName = "IMG_20251220_235914.jpg",
        )
        ShadowContentResolver.registerProviderInternal("media", provider)

        val file = PlatformFile(pickerUri)
        assertEquals(expected = "IMG_20251220_235914.jpg", actual = file.name)
        assertEquals(expected = "jpg", actual = file.extension)
        assertEquals(expected = "IMG_20251220_235914", actual = file.nameWithoutExtension)
    }

    @Test
    fun PlatformFile_name_photoPickerMediaStoreLookupFails_usesStableFallbackWithExtension() {
        val pickerUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/18")
        val provider = PhotoPickerNameContentProvider(
            pickerUri = pickerUri,
            pickerDisplayName = "18.jpg",
            mediaStoreDisplayName = null,
            throwOnMediaStoreQuery = true,
        )
        ShadowContentResolver.registerProviderInternal("media", provider)

        val file = PlatformFile(pickerUri)
        assertEquals(expected = "photopicker-18.jpg", actual = file.name)
        assertEquals(expected = "jpg", actual = file.extension)
        assertEquals(expected = "photopicker-18", actual = file.nameWithoutExtension)
    }

    @Test
    fun PlatformFile_name_photoPickerWithoutDisplayName_usesStableFallback() {
        val pickerUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/18")
        val provider = PhotoPickerNameContentProvider(
            pickerUri = pickerUri,
            pickerDisplayName = null,
            mediaStoreDisplayName = null,
            throwOnMediaStoreQuery = true,
        )
        ShadowContentResolver.registerProviderInternal("media", provider)

        val file = PlatformFile(pickerUri)
        assertEquals(expected = "photopicker-18", actual = file.name)
        assertEquals(expected = "", actual = file.extension)
        assertEquals(expected = "photopicker-18", actual = file.nameWithoutExtension)
    }

    @Test
    fun PlatformFile_name_photoPickerFallback_keepsDistinctNamesForDifferentUris() {
        val firstPickerUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/18")
        val secondPickerUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/19")
        val provider = PhotoPickerNameContentProvider(
            pickerUri = firstPickerUri,
            pickerDisplayName = "18.jpg",
            mediaStoreDisplayName = null,
            throwOnMediaStoreQuery = true,
        ).apply {
            registerPickerDisplayName(secondPickerUri, "19.jpg")
        }
        ShadowContentResolver.registerProviderInternal("media", provider)

        val firstFile = PlatformFile(firstPickerUri)
        val secondFile = PlatformFile(secondPickerUri)

        assertEquals(expected = "photopicker-18.jpg", actual = firstFile.name)
        assertEquals(expected = "photopicker-19.jpg", actual = secondFile.name)
    }

    @Test
    fun PlatformFile_name_nonPickerUri_keepsLastPathSegmentFallback() {
        initializeFileKitWithResolver(ContentResolver.wrap(NullInsertContentProvider()))

        val uri = Uri.parse("content://com.example.documents/files/report.pdf")
        val file = PlatformFile(uri)

        assertEquals(expected = "report.pdf", actual = file.name)
        assertEquals(expected = "pdf", actual = file.extension)
        assertEquals(expected = "report", actual = file.nameWithoutExtension)
    }

    @Test
    @Config(sdk = [23])
    fun PlatformFile_exists_bookmarkedTreeUriOnApi23_returnsTrue() {
        val treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        ShadowContentResolver.registerProviderInternal(
            "com.android.externalstorage.documents",
            TreeBookmarkContentProvider(),
        )

        val file = PlatformFile(treeUri)
        assertTrue(file.exists())
    }

    @Test
    @Config(sdk = [23])
    fun PlatformFile_fromBookmarkData_bookmarkedTreeUriOnApi23_restoresAccessibleFile() {
        val treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")
        ShadowContentResolver.registerProviderInternal(
            "com.android.externalstorage.documents",
            TreeBookmarkContentProvider(),
        )

        val restored = PlatformFile.fromBookmarkData(BookmarkData(treeUri.toString().encodeToByteArray()))
        assertEquals(expected = treeUri.toString(), actual = restored.path)
        assertTrue(restored.exists())
    }
}

private class NullInsertContentProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

private class NestedTreeContentProvider : ContentProvider() {
    private val documents = mutableMapOf(
        "primary:Documents" to TestDocument("primary:Documents", "Documents", true),
        "primary:Documents/Notes" to TestDocument("primary:Documents/Notes", "Notes", true),
        "primary:Documents/parent-only.txt" to TestDocument(
            "primary:Documents/parent-only.txt",
            "parent-only.txt",
            false,
        ),
        "primary:Documents/Notes/note.txt" to TestDocument("primary:Documents/Notes/note.txt", "note.txt", false),
    )

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val columns = projection?.toList()?.toTypedArray()
            ?: arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                OpenableColumns.SIZE,
            )

        val cursor = MatrixCursor(columns)
        val segments = uri.pathSegments
        val documentId = segments
            .indexOf("document")
            .takeIf { it != -1 }
            ?.let { segments.getOrNull(it + 1) }

        if (segments.lastOrNull() == "children") {
            documents
                .values
                .filter { it.parentId == documentId }
                .forEach { cursor.addDocumentRow(columns, it) }
        } else if (documentId != null) {
            documents[documentId]?.let { cursor.addDocumentRow(columns, it) }
        }

        return cursor
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != "android:createDocument") {
            return super.call(method, arg, extras)
        }

        val parentUri = extras?.getParcelableCompat(DOCUMENTS_CONTRACT_EXTRA_URI)
            ?: return null
        val parentId = DocumentsContract.getDocumentId(parentUri)
        val displayName = extras.getString(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            ?: return null
        val mimeType = extras.getString(DocumentsContract.Document.COLUMN_MIME_TYPE)
        val documentId = "$parentId/$displayName"
        val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
        documents[documentId] = TestDocument(documentId, displayName, isDirectory)
        val uri = DocumentsContract.buildDocumentUriUsingTree(parentUri, documentId)

        return Bundle().apply {
            putParcelable(DOCUMENTS_CONTRACT_EXTRA_URI, uri)
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

private fun createNestedTreeContentProvider(): NestedTreeContentProvider =
    NestedTreeContentProvider().apply {
        attachInfo(
            RuntimeEnvironment.getApplication(),
            ProviderInfo().apply {
                authority = "com.android.externalstorage.documents"
            },
        )
    }

private fun MatrixCursor.addDocumentRow(
    columns: Array<out String>,
    document: TestDocument,
) {
    val row = columns
        .map { column ->
            when (column) {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID -> document.id

                DocumentsContract.Document.COLUMN_DISPLAY_NAME -> document.name

                DocumentsContract.Document.COLUMN_MIME_TYPE -> if (document.isDirectory) {
                    DocumentsContract.Document.MIME_TYPE_DIR
                } else {
                    "text/plain"
                }

                OpenableColumns.SIZE -> null

                else -> null
            }
        }.toTypedArray()
    addRow(row)
}

private data class TestDocument(
    val id: String,
    val name: String,
    val isDirectory: Boolean,
) {
    val parentId: String?
        get() = id.substringBeforeLast('/', missingDelimiterValue = "").takeIf(String::isNotEmpty)
}

@Suppress("DEPRECATION")
private fun Bundle.getParcelableCompat(key: String): Uri? =
    getParcelable(key)

private const val DOCUMENTS_CONTRACT_EXTRA_URI = "uri"

private class MissingSizeContentProvider(
    private val sourceUri: Uri,
    private val destinationUri: Uri,
    private val sourceBytes: ByteArray,
) : ContentProvider() {
    private val filesByUri = mutableMapOf<String, File>()

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME))
        cursor.addRow(arrayOf(fileFor(uri).name))
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val file = fileFor(uri)
        return ParcelFileDescriptor.open(file, mode.toParcelFlags())
    }

    private fun fileFor(uri: Uri): File = synchronized(filesByUri) {
        filesByUri.getOrPut(uri.toString()) {
            val prefix = (uri.lastPathSegment ?: "file")
                .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                .let { if (it.length >= 3) it else it.padEnd(3, '_') }
            val tempFile = File.createTempFile("provider-$prefix-", ".bin")
            when (uri.toString()) {
                sourceUri.toString() -> tempFile.writeBytes(sourceBytes)
                destinationUri.toString() -> tempFile.writeBytes(byteArrayOf())
                else -> tempFile.writeBytes(byteArrayOf())
            }
            tempFile.deleteOnExit()
            tempFile
        }
    }

    private fun String.toParcelFlags(): Int = when (this) {
        "r" -> {
            ParcelFileDescriptor.MODE_READ_ONLY
        }

        "w", "wt" -> {
            ParcelFileDescriptor.MODE_WRITE_ONLY or
                ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_TRUNCATE
        }

        "wa" -> {
            ParcelFileDescriptor.MODE_WRITE_ONLY or
                ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_APPEND
        }

        "rw" -> {
            ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
        }

        "rwt" -> {
            ParcelFileDescriptor.MODE_READ_WRITE or
                ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_TRUNCATE
        }

        else -> {
            throw FileNotFoundException("Unsupported mode: $this")
        }
    }
}

private class PhotoPickerNameContentProvider(
    pickerUri: Uri,
    pickerDisplayName: String?,
    private val mediaStoreDisplayName: String?,
    private val throwOnMediaStoreQuery: Boolean = false,
) : ContentProvider() {
    private val pickerDisplayNamesByUri = mutableMapOf(pickerUri to pickerDisplayName)

    fun registerPickerDisplayName(uri: Uri, displayName: String?) {
        pickerDisplayNamesByUri[uri] = displayName
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor = when {
        pickerDisplayNamesByUri.containsKey(uri) -> {
            MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply {
                addRow(arrayOf(pickerDisplayNamesByUri.getValue(uri), null))
            }
        }

        uri.isMediaStoreLookupUri() -> {
            if (throwOnMediaStoreQuery) {
                throw SecurityException("MediaStore lookup not allowed")
            }

            val requestedId = selectionArgs?.firstOrNull()?.toLongOrNull()
            val pickerIds = pickerDisplayNamesByUri.keys.mapNotNull { it.lastPathSegment?.toLongOrNull() }
            MatrixCursor(arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)).apply {
                if (requestedId != null && requestedId in pickerIds && mediaStoreDisplayName != null) {
                    addRow(arrayOf(mediaStoreDisplayName))
                }
            }
        }

        else -> {
            MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME))
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}

private fun Uri.isMediaStoreLookupUri(): Boolean {
    if (authority != "media") {
        return false
    }
    val segments = pathSegments
    return segments.isNotEmpty() && segments[0].startsWith("external")
}

private class TreeBookmarkContentProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val columns = projection?.toList()?.toTypedArray()
            ?: arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                OpenableColumns.SIZE,
            )

        val cursor = MatrixCursor(columns)
        val isDocumentUri = uri.path?.contains("/document/") == true
        if (isDocumentUri) {
            val row = columns
                .map { column ->
                    when (column) {
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID -> "primary:Documents"
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME -> "Documents"
                        DocumentsContract.Document.COLUMN_MIME_TYPE -> DocumentsContract.Document.MIME_TYPE_DIR
                        OpenableColumns.SIZE -> null
                        else -> null
                    }
                }.toTypedArray()
            cursor.addRow(row)
        }
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
