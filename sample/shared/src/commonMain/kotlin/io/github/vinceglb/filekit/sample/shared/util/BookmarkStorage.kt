package io.github.vinceglb.filekit.sample.shared.util

import io.github.vinceglb.filekit.PlatformFile

internal enum class BookmarkKind {
    File,
    Directory,
}

internal expect class BookmarkStorage() {
    val isSupported: Boolean

    suspend fun load(kind: BookmarkKind): PlatformFile?

    suspend fun save(kind: BookmarkKind, file: PlatformFile?)
}
