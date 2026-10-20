@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.DocumentsContract
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Issue #655: exercises real FileKit navigation against a simulated SAF tree grant. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlatformFileParentAndroidTest {
    @Before
    fun setup() {
        ShadowContentResolver.reset()
        FileKit.manualFileKitCoreInitialization(RuntimeEnvironment.getApplication())
    }

    @Test
    fun PlatformFile_parent_childOfDocuments_listsSelectedDirectory() {
        val selected = selectTree("primary:Documents")
        val child = selected.list().single { it.name == "Notes" }

        val parent = assertNotNull(child.parent())

        assertEquals(selected.list().map { it.name }, parent.list().map { it.name })
    }

    @Test
    fun PlatformFile_parent_childOfNestedSelection_listsSelectedDirectory() {
        val selected = selectTree("primary:Documents/Notes")
        val child = selected.list().single { it.name == "Drafts" }
        assertEquals(listOf("note.txt"), child.list().map { it.name })

        val parent = assertNotNull(child.parent())

        assertEquals(selected.list().map { it.name }, parent.list().map { it.name })
    }

    @Test
    fun PlatformFile_parent_grandchildOfDocuments_returnsImmediateParent() {
        val selected = selectTree("primary:Documents")
        val notes = selected.list().single { it.name == "Notes" }
        val drafts = notes.list().single { it.name == "Drafts" }

        val parent = assertNotNull(drafts.parent())

        assertEquals(notes.path, parent.path)
        assertEquals(notes.list().map { it.name }, parent.list().map { it.name })
    }

    @Test
    fun PlatformFile_list_documentUriOfNestedTreeRoot_listsSelectedDirectory() {
        val selected = selectTree("primary:Documents/Notes")
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            Uri.parse(selected.path),
            "primary:Documents/Notes",
        )

        val root = PlatformFile(documentUri)

        // Both URI forms identify the same selected directory, with the same grant.
        assertTrue(root.exists())
        assertTrue(root.isDirectory())
        assertFalse(root.isRegularFile())
        assertEquals(selected.list().map { it.name }, root.list().map { it.name })
    }

    @Test
    @Config(sdk = [24])
    fun PlatformFile_parent_childOfNestedSelectionOnApi24_listsSelectedDirectory() {
        val selected = selectTree("primary:Documents/Notes")
        val child = selected.list().single { it.name == "Drafts" }

        val parent = assertNotNull(child.parent())

        assertEquals(selected.list().map { it.name }, parent.list().map { it.name })
    }

    @Test
    fun PlatformFile_parent_selectedRootInEitherUriForm_returnsNull() {
        val selected = selectTree("primary:Documents/Notes")
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            Uri.parse(selected.path),
            "primary:Documents/Notes",
        )

        assertNull(selected.parent())
        assertNull(PlatformFile(documentUri).parent())
    }

    @Test
    fun PlatformFile_parent_fileWithEncodedCharacters_preservesParentAndGrant() {
        val treeId = "primary:Documents/Notes #1%"
        val parentId = "$treeId/été + drafts"
        val treeUri = DocumentsContract.buildTreeDocumentUri(PARENT_TEST_AUTHORITY, treeId)
        val file = PlatformFile(DocumentsContract.buildDocumentUriUsingTree(treeUri, "$parentId/note.txt"))

        val parent = assertNotNull(file.parent())

        assertEquals(DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId).toString(), parent.path)
    }

    @Test
    fun PlatformFile_parent_childOfStorageVolumeRoot_returnsGrantedRoot() {
        val treeUri = DocumentsContract.buildTreeDocumentUri(PARENT_TEST_AUTHORITY, "1234-ABCD:")
        val child = PlatformFile(DocumentsContract.buildDocumentUriUsingTree(treeUri, "1234-ABCD:Notes"))

        val parent = assertNotNull(child.parent())

        assertEquals(DocumentsContract.buildDocumentUriUsingTree(treeUri, "1234-ABCD:").toString(), parent.path)
        assertNull(parent.parent())
    }

    @Test
    fun PlatformFile_parent_documentOutsideGrantedTree_returnsNull() {
        val treeUri = DocumentsContract.buildTreeDocumentUri(PARENT_TEST_AUTHORITY, "primary:Documents/Notes")
        val outside = PlatformFile(
            DocumentsContract.buildDocumentUriUsingTree(treeUri, "primary:Documents/NotesOther/note.txt"),
        )

        assertNull(outside.parent())
    }

    @Test
    fun PlatformFile_parent_nonTreeContentUri_returnsNull() {
        val document = PlatformFile(DocumentsContract.buildDocumentUri(PARENT_TEST_AUTHORITY, "primary:Documents/note.txt"))
        val media = PlatformFile(Uri.parse("content://media/external/images/media/42"))

        assertNull(document.parent())
        assertNull(media.parent())
    }

    @Test
    fun PlatformFile_parent_opaqueProviderIds_returnsNull() {
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.example.documents", "root")
        val child = PlatformFile(DocumentsContract.buildDocumentUriUsingTree(treeUri, "root/opaque-id"))

        assertNull(child.parent())
    }

    private fun selectTree(documentId: String): PlatformFile {
        val provider = ParentNavigationProvider(documentId).apply {
            attachInfo(
                RuntimeEnvironment.getApplication(),
                ProviderInfo().apply { authority = PARENT_TEST_AUTHORITY },
            )
        }
        ShadowContentResolver.registerProviderInternal(PARENT_TEST_AUTHORITY, provider)
        return PlatformFile(DocumentsContract.buildTreeDocumentUri(PARENT_TEST_AUTHORITY, documentId))
    }
}

/**
 * Models a picker grant to one tree only. Robolectric does not enforce Android's
 * URI grants here, so the provider rejects queries using a different tree or
 * targeting documents outside the selected tree. It does not mock parent/list.
 */
private class ParentNavigationProvider(
    private val grantedTreeId: String,
) : ContentProvider() {
    private val documents = linkedMapOf(
        "primary:Documents" to true,
        "primary:Documents/Notes" to true,
        "primary:Documents/Notes/Drafts" to true,
        "primary:Documents/Notes/Drafts/note.txt" to false,
    )

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val documentId = DocumentsContract.getDocumentId(uri)
        if (DocumentsContract.getTreeDocumentId(uri) != grantedTreeId ||
            (documentId != grantedTreeId && !documentId.startsWith("$grantedTreeId/"))
        ) {
            throw SecurityException("No picker grant for $uri")
        }

        val columns = projection ?: arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        return MatrixCursor(columns).apply {
            documents.forEach { (id, isDirectory) ->
                val matches = if (uri.lastPathSegment == "children") {
                    id.substringBeforeLast('/', "") == documentId
                } else {
                    id == documentId
                }
                if (matches) {
                    addRow(
                        columns
                            .map { column ->
                                when (column) {
                                    DocumentsContract.Document.COLUMN_DOCUMENT_ID -> id

                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME -> id.substringAfterLast('/').substringAfter(':')

                                    DocumentsContract.Document.COLUMN_MIME_TYPE -> if (isDirectory) {
                                        DocumentsContract.Document.MIME_TYPE_DIR
                                    } else {
                                        "text/plain"
                                    }

                                    else -> null
                                }
                            }.toTypedArray(),
                    )
                }
            }
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

private const val PARENT_TEST_AUTHORITY = "com.android.externalstorage.documents"
