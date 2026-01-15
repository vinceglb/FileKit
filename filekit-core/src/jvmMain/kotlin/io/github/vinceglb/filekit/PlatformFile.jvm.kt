package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toKotlinxIoPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents a file on the JVM platform.
 *
 * @property file The underlying [java.io.File] object.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual data class PlatformFile(
    val file: File,
) {
    public actual override fun toString(): String = path

    public actual companion object
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(path.toFile())

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    file.toKotlinxIoPath()

public actual val PlatformFile.extension: String
    get() = file.extension

public actual val PlatformFile.nameWithoutExtension: String
    get() = file.nameWithoutExtension

public actual fun PlatformFile.absolutePath(): String =
    file.absolutePath

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
    }

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? = runCatching {
    val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
    val timestamp = attributes.creationTime().toMillis()
    return Instant.fromEpochMilliseconds(timestamp)
}.getOrNull()

@OptIn(ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant {
    val timestamp = this.file.lastModified()
    return Instant.fromEpochMilliseconds(timestamp)
}

public actual fun PlatformFile.mimeType(): MimeType? {
    val mimeTypeValue = try {
        Files.probeContentType(file.toPath())
    } catch (_: Exception) {
        null
    }

    return mimeTypeValue?.let(MimeType::parse)
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    BookmarkData(file.path.encodeToByteArray())
}

public actual fun PlatformFile.releaseBookmark() {}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile {
    val path = bookmarkData.bytes.decodeToString()
    return PlatformFile(Path(path))
}
