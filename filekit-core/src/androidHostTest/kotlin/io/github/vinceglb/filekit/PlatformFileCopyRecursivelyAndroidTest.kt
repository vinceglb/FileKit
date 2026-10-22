@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import android.Manifest
import android.content.ComponentName
import android.content.IntentFilter
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.provider.DocumentsProvider
import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 36], shadows = [NoFollowOsShadow::class, DocumentsProviderQueryShadow::class])
class PlatformFileCopyRecursivelyAndroidTest {
    private lateinit var temporaryDirectory: File
    private lateinit var provider: CopyDocumentsProvider

    @Before
    fun setup() {
        ShadowContentResolver.reset()
        val context = RuntimeEnvironment.getApplication()
        FileKit.manualFileKitCoreInitialization(context)
        temporaryDirectory = Files.createTempDirectory("filekit-copy-saf").toFile()
        val info = ProviderInfo().apply {
            authority = COPY_TEST_AUTHORITY
            packageName = context.packageName
            name = CopyDocumentsProvider::class.java.name
            applicationInfo = context.applicationInfo
            enabled = true
            exported = true
            grantUriPermissions = true
            readPermission = Manifest.permission.MANAGE_DOCUMENTS
            writePermission = Manifest.permission.MANAGE_DOCUMENTS
        }
        Shadows.shadowOf(context.packageManager).apply {
            addOrUpdateProvider(info)
            addIntentFilterForProvider(ComponentName(info.packageName, info.name), IntentFilter(DocumentsContract.PROVIDER_INTERFACE))
        }
        provider = CopyDocumentsProvider(temporaryDirectory.resolve("documents")).apply {
            attachInfo(context, info)
        }
        ShadowContentResolver.registerProviderInternal(COPY_TEST_AUTHORITY, provider)
    }

    @After
    fun cleanup() {
        ShadowContentResolver.reset()
        temporaryDirectory.deleteRecursively()
    }

    @Test
    fun PlatformFile_copyRecursivelyOpaqueDocumentTree_preservesNamesBytesAndEmptyDirectories() = runTest {
        val source = createOpaqueTree()
        val target = PlatformFile(temporaryDirectory.resolve("copied"))

        source.copyRecursively(target)

        assertEquals(setOf("été + drafts", "empty folder", "top.txt"), target.list().map { it.name }.toSet())
        assertEquals("top content", (target / "top.txt").readString())
        assertContentEquals(byteArrayOf(0, -1, 127, -128), (target / "été + drafts" / "note #1%.bin").readBytes())
        assertTrue((target / "empty folder").isDirectory())
        assertTrue((target / "empty folder").list().isEmpty())
        assertContentEquals(byteArrayOf(0, -1, 127, -128), document("root", "file-2").readBytes())
        assertEquals("top content", document("root", "file-3").readString())
        assertFalse((target / "selected folder").exists())
    }

    @Test
    fun PlatformFile_copyRecursivelyDocumentFileToMissingPath_createsRenamedCopyAndPreservesSource() = runTest {
        createOpaqueTree()
        val source = document("root", "file-3")
        val target = PlatformFile(temporaryDirectory.resolve("missing/nested/renamed.txt"))

        source.copyRecursively(target)

        assertEquals("top content", target.readString())
        assertEquals("top content", source.readString())
    }

    @Test
    fun PlatformFile_copyRecursivelyFileToDocument_createsFileAndTruncatesOnOverwrite() = runTest {
        provider.add("primary:target", "selected target")
        val target = tree("primary:target") / "renamed.txt"
        val source = PlatformFile(temporaryDirectory.resolve("source.txt"))
        source.writeString("long original content")

        source.copyRecursively(target)
        assertEquals("long original content", target.readString())

        source.writeString("new")
        source.copyRecursively(target, overwrite = true)

        assertEquals("new", target.readString())
        assertEquals("new", source.readString())
        assertEquals(listOf("renamed.txt"), tree("primary:target").list().map { it.name })
    }

    @Test
    fun PlatformFile_copyRecursivelyDocumentTreeWithoutOverwrite_skipsConflictAndCopiesNewFiles() = runTest {
        val source = createOpaqueTree()
        provider.add("primary:target", "selected target")
        provider.add("primary:target/top.txt", "top.txt", "primary:target", "original".encodeToByteArray())
        val target = tree("primary:target")
        var skippedCalled = false
        var addedCalled = false
        var overwrittenCalled = false

        source.copyRecursively(
            target = target,
            onFileSkipped = { skippedCalled = true },
            onFileAdded = { addedCalled = true },
            onFileOverwritten = { overwrittenCalled = true },
        )

        assertEquals("original", (target / "top.txt").readString())
        assertContentEquals(byteArrayOf(0, -1, 127, -128), (target / "été + drafts" / "note #1%.bin").readBytes())
        assertTrue((target / "empty folder").list().isEmpty())
        assertTrue(skippedCalled)
        assertTrue(addedCalled)
        assertFalse(overwrittenCalled)
        assertEquals("top content", document("root", "file-3").readString())
    }

    @Test
    fun PlatformFile_copyRecursivelyDocumentReadFailure_keepsCompletedFilesAndStops() = runTest {
        provider.add("root", "selected folder")
        provider.add("first", "first.txt", "root", "first content".encodeToByteArray())
        provider.add("broken", "broken.txt", "root", "broken content".encodeToByteArray())
        provider.add("last", "last.txt", "root", "last content".encodeToByteArray())
        provider.unreadableDocumentId = "broken"
        val target = PlatformFile(temporaryDirectory.resolve("copied"))

        assertFailsWith<FileNotFoundException> { tree("root").copyRecursively(target) }

        assertEquals("first content", (target / "first.txt").readString())
        assertFalse((target / "last.txt").exists())
        provider.unreadableDocumentId = null
        assertEquals("broken content", document("root", "broken").readString())
        assertEquals("last content", document("root", "last").readString())
    }

    @Test
    fun PlatformFile_resolveRelativePathNestedDocumentRoot_excludesAncestors() {
        createOpaqueTree()
        provider.add("opaque/id:not-a-name", "another.txt", "dir-1", byteArrayOf())

        assertEquals("note #1%.bin", document("root", "file-2").resolveRelativePath(document("root", "dir-1")))
        assertEquals("été + drafts/another.txt", document("root", "opaque/id:not-a-name").resolveRelativePath(tree("root")))
        assertEquals("", document("root", "root").resolveRelativePath(tree("root")))
    }

    @Test
    fun PlatformFile_resolveRelativePathIncompatibleRoot_throws() {
        createOpaqueTree()
        val source = document("root", "file-2")
        val otherProvider = PlatformFile(DocumentsContract.buildTreeDocumentUri("com.example.other.documents", "root"))

        assertFailsWith<IllegalArgumentException> { source.resolveRelativePath(otherProvider) }
        assertFailsWith<IllegalArgumentException> { source.resolveRelativePath(PlatformFile(temporaryDirectory)) }
    }

    @Test
    fun PlatformFile_resolveRelativePathOutsideSelectedSubtree_throws() {
        createOpaqueTree()

        assertFailsWith<IllegalArgumentException> {
            document("root", "file-3").resolveRelativePath(document("root", "dir-1"))
        }
    }

    @Test
    fun PlatformFile_resolveRelativePathDeniedQuery_propagatesPermissionFailure() {
        createOpaqueTree()
        provider.deniedDirectoryId = "dir-1"

        assertFailsWith<SecurityException> { document("root", "file-2").resolveRelativePath(tree("root")) }
    }

    @Test
    fun PlatformFile_resolveRelativePathNullCursor_throwsInsteadOfTreatingTreeAsEmpty() {
        createOpaqueTree()
        provider.nullCursorDirectoryId = "root"

        assertFailsWith<FileKitException> { document("root", "file-2").resolveRelativePath(tree("root")) }
    }

    @Test
    fun PlatformFile_resolveRelativePathMissingMetadata_throws() {
        createOpaqueTree()
        for (column in listOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE)) {
            provider.missingColumn = column
            assertFailsWith<IllegalArgumentException> { document("root", "file-2").resolveRelativePath(tree("root")) }
        }
    }

    @Test(timeout = 5_000)
    fun PlatformFile_resolveRelativePathCyclicTree_terminatesWhenSourceIsAbsent() {
        createOpaqueTree()
        provider.cyclicDirectoryId = "dir-1"

        assertFailsWith<IllegalArgumentException> { document("root", "absent").resolveRelativePath(tree("root")) }
    }

    private fun createOpaqueTree(): PlatformFile {
        provider.add("root", "selected folder")
        provider.add("dir-1", "été + drafts", "root")
        provider.add("file-2", "note #1%.bin", "dir-1", byteArrayOf(0, -1, 127, -128))
        provider.add("empty-4", "empty folder", "root")
        provider.add("file-3", "top.txt", "root", "top content".encodeToByteArray())
        return tree("root")
    }

    private fun tree(id: String): PlatformFile = PlatformFile(DocumentsContract.buildTreeDocumentUri(COPY_TEST_AUTHORITY, id))

    private fun document(treeId: String, id: String): PlatformFile = PlatformFile(
        DocumentsContract.buildDocumentUriUsingTree(DocumentsContract.buildTreeDocumentUri(COPY_TEST_AUTHORITY, treeId), id),
    )
}

/**
 * Executes FileKit against Android's document API and real file descriptors. Source IDs
 * are opaque; created destination IDs follow the external-storage provider's path scheme,
 * which is the destination profile currently supported by FileKit. Robolectric does not
 * enforce picker permissions, so denied queries are injected at the provider boundary.
 */
private class CopyDocumentsProvider(
    private val storage: File,
) : DocumentsProvider() {
    private data class Entry(
        val id: String,
        val name: String,
        val parentId: String?,
        val bytes: File?,
    )

    private val entries = linkedMapOf<String, Entry>()
    var unreadableDocumentId: String? = null
    var deniedDirectoryId: String? = null
    var nullCursorDirectoryId: String? = null
    var missingColumn: String? = null
    var cyclicDirectoryId: String? = null

    override fun onCreate(): Boolean = true

    fun add(id: String, name: String, parentId: String? = null, content: ByteArray? = null) {
        val bytes = content?.let {
            storage.mkdirs()
            File.createTempFile("document", ".bin", storage).apply { writeBytes(it) }
        }
        entries[id] = Entry(id, name, parentId, bytes)
    }

    override fun queryRoots(projection: Array<out String>?): Cursor = MatrixCursor(projection ?: emptyArray())

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor =
        cursor(projection, listOfNotNull(entries[documentId]))

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<out String>?, sortOrder: String?): Cursor? {
        if (parentDocumentId == deniedDirectoryId) throw SecurityException("Picker access revoked")
        if (parentDocumentId == nullCursorDirectoryId) return null
        val children = entries.values.filter { it.parentId == parentDocumentId }.toMutableList()
        if (parentDocumentId == cyclicDirectoryId) children.add(entries.getValue(parentDocumentId))
        return cursor(projection, children)
    }

    override fun isChildDocument(parentDocumentId: String, documentId: String): Boolean {
        var parentId = entries[documentId]?.parentId
        while (parentId != null) {
            if (parentId == parentDocumentId) return true
            parentId = entries[parentId]?.parentId
        }
        return false
    }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        require(entries[parentDocumentId]?.bytes == null && entries.containsKey(parentDocumentId))
        val id = "$parentDocumentId/$displayName"
        check(id !in entries)
        add(id, displayName, parentDocumentId, if (mimeType == Document.MIME_TYPE_DIR) null else byteArrayOf())
        return id
    }

    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor {
        if (documentId == unreadableDocumentId) throw FileNotFoundException("Document cannot be opened")
        val file = entries[documentId]?.bytes ?: throw FileNotFoundException(documentId)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }

    private fun cursor(projection: Array<out String>?, selected: List<Entry>): Cursor {
        val columns = projection ?: arrayOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE)
        return MatrixCursor(columns).apply {
            for (entry in selected) {
                addRow(
                    columns
                        .map { column ->
                            if (column == missingColumn) {
                                null
                            } else {
                                when (column) {
                                    Document.COLUMN_DOCUMENT_ID -> {
                                        entry.id
                                    }

                                    Document.COLUMN_DISPLAY_NAME -> {
                                        entry.name
                                    }

                                    Document.COLUMN_MIME_TYPE -> {
                                        if (entry.bytes == null) {
                                            Document.MIME_TYPE_DIR
                                        } else {
                                            "application/octet-stream"
                                        }
                                    }

                                    Document.COLUMN_SIZE -> {
                                        entry.bytes?.length() ?: 0L
                                    }

                                    Document.COLUMN_FLAGS -> {
                                        Document.FLAG_SUPPORTS_WRITE or Document.FLAG_DIR_SUPPORTS_CREATE
                                    }

                                    else -> {
                                        null
                                    }
                                }
                            }
                        }.toTypedArray<Any?>(),
                )
            }
        }
    }
}

private const val COPY_TEST_AUTHORITY = "com.android.externalstorage.documents"

// Robolectric forwards the legacy query directly to the provider. Android's resolver
// converts it to the Bundle overload on API 26+, where DocumentsProvider requires it.
@Implements(DocumentsProvider::class, minSdk = 26)
class DocumentsProviderQueryShadow {
    @RealObject
    private lateinit var provider: DocumentsProvider

    @Implementation(minSdk = 26)
    fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = provider.query(uri, projection, Bundle.EMPTY, null)
}
