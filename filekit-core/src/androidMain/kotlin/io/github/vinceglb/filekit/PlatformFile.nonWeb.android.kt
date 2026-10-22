package io.github.vinceglb.filekit

import android.net.Uri
import android.provider.DocumentsContract
import io.github.vinceglb.filekit.exceptions.FileKitException

/**
 * Resolves this source entry's relative path for an Android filesystem or document tree.
 *
 * Filesystem paths use the absolute directory prefix. Document URIs are located by
 * visiting the granted source tree and joining display names, without interpreting
 * document IDs as paths. The provider must expose readable child document metadata.
 *
 * @param root The source filesystem directory or a document directory from the same provider.
 * @return The child path below [root]. For document URIs, returns an empty string when
 * this entry and [root] identify the same document.
 * @throws IllegalArgumentException If a document source has an incompatible root,
 * cannot be found below it, or the provider omits required metadata. Filesystem paths
 * must start with the absolute directory prefix of [root].
 * @throws FileKitException If a child document query returns no cursor.
 */
internal actual fun PlatformFile.resolveRelativePath(
    root: PlatformFile,
): String = when (val file = androidFile) {
    is AndroidFile.FileWrapper -> {
        resolveRelativePathForFileSystem(
            root = root,
        )
    }

    is AndroidFile.UriWrapper -> {
        val rootUri = (root.androidFile as? AndroidFile.UriWrapper)?.uri
            ?: throw IllegalArgumentException("Root must be a document URI")
        file.uri.resolveRelativePath(rootUri)
    }
}

/**
 * Finds this document below [root] and derives its relative path from display names.
 *
 * Searches the granted tree without interpreting document IDs as filesystem paths.
 * Query failures propagate, and already visited directory IDs are not searched again.
 *
 * @param root The source document directory from the same provider.
 * @return The relative display-name path, or an empty string for the root document itself.
 * @throws IllegalArgumentException If the providers differ, the document is outside
 * the root tree, or required document metadata is missing.
 * @throws FileKitException If a child document query returns no cursor.
 */
private fun Uri.resolveRelativePath(root: Uri): String {
    require(authority == root.authority) {
        "Source and root must belong to the same document provider"
    }

    val sourceId = documentId()
    val rootId = root.documentId()
    if (sourceId == rootId) return ""

    val visited = mutableSetOf<String>()

    fun findRelativePath(parentId: String, prefix: String): String? {
        if (!visited.add(parentId)) return null

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(root, parentId)
        val cursor = FileKit.context.contentResolver.query(
            childrenUri,
            ANDROID_DOCUMENT_INFO_PROJECTION,
            null,
            null,
            null,
        ) ?: throw FileKitException("Could not list documents: $childrenUri")

        cursor.use { documents ->
            val documentIdIndex = documents.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = documents.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = documents.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (documents.moveToNext()) {
                val documentId = requireNotNull(documents.getString(documentIdIndex)) { "Missing document ID" }
                val name = requireNotNull(documents.getString(nameIndex)) { "Missing document name" }
                val mimeType = requireNotNull(documents.getString(mimeTypeIndex)) { "Missing document MIME type" }
                val relativePath = "$prefix$name"

                if (documentId == sourceId) return relativePath
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    findRelativePath(documentId, "$relativePath/")?.let { return it }
                }
            }
        }
        return null
    }

    return findRelativePath(parentId = rootId, prefix = "")
        ?: throw IllegalArgumentException("Source document is outside the root directory")
}
