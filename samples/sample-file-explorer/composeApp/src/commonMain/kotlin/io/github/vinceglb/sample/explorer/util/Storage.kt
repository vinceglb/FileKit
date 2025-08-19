package io.github.vinceglb.sample.explorer.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.fromBookmarkData
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write

class Storage {
    private val previousFolder = FileKit.filesDir / "saved_directory_data.bin"

    suspend fun saveBookmark(platformFile: PlatformFile?) {
        when (platformFile) {
            null -> previousFolder.delete()
            else -> {
                val bookmark = platformFile.bookmarkData()
                previousFolder.write(bookmark.bytes)
            }
        }
    }

    suspend fun retrieveFromBookmark(): PlatformFile? {
        return if (previousFolder.exists()) {
            val bytes = previousFolder.readBytes()
            PlatformFile.fromBookmarkData(bytes)
        } else {
            null
        }
    }
}

@Composable
fun rememberStorage(): Storage {
    return remember { Storage() }
} 