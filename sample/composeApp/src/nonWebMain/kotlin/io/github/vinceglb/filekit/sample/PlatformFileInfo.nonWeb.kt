package io.github.vinceglb.filekit.sample

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.bookmarkData
import io.github.vinceglb.filekit.createdAt
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.isAbsolute
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.isRegularFile
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.parent
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.size
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
actual suspend fun getPlatformFileInfo(file: PlatformFile): List<InfoRow> {
    val rows = mutableListOf<InfoRow>()

    rows += InfoRow("name", file.name)
    rows += InfoRow("extension", file.extension)
    rows += InfoRow("nameWithoutExtension", file.nameWithoutExtension)
    rows += InfoRow("path", file.path)
    rows += InfoRow("absolutePath", file.absolutePath())
    rows += InfoRow("exists", file.exists().toString())
    rows += InfoRow("isDirectory", file.isDirectory().toString())
    rows += InfoRow("isRegularFile", file.isRegularFile().toString())
    rows += InfoRow("isAbsolute", file.isAbsolute().toString())
    rows += InfoRow("sizeBytes", file.size().toString())
    rows += InfoRow("mimeType", file.mimeType()?.toString() ?: "unknown")
    rows += InfoRow("parent", file.parent()?.path ?: "none")
    rows += InfoRow("createdAt", file.createdAt()?.toString() ?: "unknown")
    rows += InfoRow("lastModified", file.lastModified().toString())

    val bookmarkSize = runCatching { file.bookmarkData().bytes.size }.getOrNull()
    if (bookmarkSize != null) {
        rows += InfoRow("bookmarkDataBytes", bookmarkSize.toString())
    }

    return rows
}
