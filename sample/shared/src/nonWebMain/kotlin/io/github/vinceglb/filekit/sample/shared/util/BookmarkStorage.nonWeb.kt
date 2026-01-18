package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.fromBookmarkData
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.releaseBookmark
import io.github.vinceglb.filekit.write

internal actual class BookmarkStorage actual constructor() {
    actual val isSupported: Boolean = true

    private val bookmarksDirectory = FileKit.filesDir / "sample-bookmarks"
    private val fileBookmark = bookmarksDirectory / "bookmarked-file.bin"
    private val directoryBookmark = bookmarksDirectory / "bookmarked-directory.bin"

    actual suspend fun load(kind: BookmarkKind): PlatformFile? {
        val bookmarkFile = resolveBookmarkFile(kind)
        if (!bookmarkFile.exists()) {
            return null
        }

        return try {
            val bytes = bookmarkFile.readBytes()
            PlatformFile.fromBookmarkData(bytes)
        } catch (_: Throwable) {
            bookmarkFile.delete(mustExist = false)
            null
        }
    }

    actual suspend fun save(kind: BookmarkKind, file: PlatformFile?) {
        val bookmarkFile = resolveBookmarkFile(kind)
        if (file == null) {
            clearBookmark(bookmarkFile)
            return
        }

        bookmarksDirectory.createDirectories()
        val bookmark = file.bookmarkData()
        bookmarkFile write bookmark.bytes
    }

    private suspend fun clearBookmark(bookmarkFile: PlatformFile) {
        if (!bookmarkFile.exists()) {
            return
        }

        try {
            val bytes = bookmarkFile.readBytes()
            val bookmarkedFile = PlatformFile.fromBookmarkData(bytes)
            bookmarkedFile.releaseBookmark()
        } catch (_: Throwable) {
            // Ignore failures from stale bookmarks.
        }

        bookmarkFile.delete(mustExist = false)
    }

    private fun resolveBookmarkFile(kind: BookmarkKind): PlatformFile = when (kind) {
        BookmarkKind.File -> fileBookmark
        BookmarkKind.Directory -> directoryBookmark
    }
}
