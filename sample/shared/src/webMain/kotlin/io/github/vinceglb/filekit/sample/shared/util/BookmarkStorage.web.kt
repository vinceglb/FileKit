package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile

internal actual class BookmarkStorage actual constructor() {
    actual val isSupported: Boolean = false

    actual suspend fun load(kind: BookmarkKind): PlatformFile? = null

    actual suspend fun save(kind: BookmarkKind, file: PlatformFile?) {
        // Bookmark data is not supported on web targets.
    }
}
