package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

public expect fun PlatformFile(path: Path): PlatformFile

public expect fun PlatformFile(path: String): PlatformFile

public fun PlatformFile(base: PlatformFile, child: String): PlatformFile =
    PlatformFile(base.toKotlinxIoPath() / child)

public expect fun PlatformFile.toKotlinxIoPath(): Path

public expect val PlatformFile.path: String

public expect fun PlatformFile.parent(): PlatformFile?

public expect fun PlatformFile.absolutePath(): String

public expect fun PlatformFile.absoluteFile(): PlatformFile

public expect fun PlatformFile.source(): RawSource

public expect fun PlatformFile.sink(append: Boolean = false): RawSink

public expect fun PlatformFile.isRegularFile(): Boolean

public expect fun PlatformFile.isDirectory(): Boolean

public expect fun PlatformFile.isAbsolute(): Boolean

public expect fun PlatformFile.exists(): Boolean

@OptIn(ExperimentalTime::class)
public expect fun PlatformFile.createdAt(): Instant?

@OptIn(ExperimentalTime::class)
public expect fun PlatformFile.lastModified(): Instant

public actual suspend fun PlatformFile.readBytes(): ByteArray =
    withContext(Dispatchers.IO) {
        this@readBytes
            .source()
            .buffered()
            .use { it.readByteArray() }
    }

public actual suspend fun PlatformFile.readString(): String =
    withContext(Dispatchers.IO) {
        this@readString
            .source()
            .buffered()
            .use { it.readString() }
    }

public suspend infix fun PlatformFile.write(bytes: ByteArray): Unit =
    withContext(Dispatchers.IO) {
        this@write
            .sink()
            .buffered()
            .use { it.write(bytes) }
    }

public suspend infix fun PlatformFile.write(platformFile: PlatformFile): Unit =
    withContext(Dispatchers.IO) {
        platformFile.source().use { source ->
            val size = platformFile.size()
            this@write
                .sink()
                .buffered()
                .use { it.write(source, size) }
        }
    }

public suspend fun PlatformFile.writeString(string: String): Unit =
    withContext(Dispatchers.IO) {
        this@writeString
            .sink()
            .buffered()
            .use { it.writeString(string) }
    }

public suspend infix fun PlatformFile.copyTo(destination: PlatformFile): Unit =
    destination write this

public fun PlatformFile.createDirectories(mustCreate: Boolean = false): Unit =
    SystemFileSystem.createDirectories(toKotlinxIoPath(), mustCreate)

public expect inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit)

public expect fun PlatformFile.list(): List<PlatformFile>

public expect suspend fun PlatformFile.atomicMove(destination: PlatformFile)

public expect suspend fun PlatformFile.delete(mustExist: Boolean = true)

public operator fun PlatformFile.div(child: String): PlatformFile =
    PlatformFile(this, child)

public fun PlatformFile.resolve(relative: String): PlatformFile =
    this / relative

public expect suspend fun PlatformFile.bookmarkData(): BookmarkData

public expect fun PlatformFile.releaseBookmark()

public expect fun PlatformFile.Companion.fromBookmarkData(bookmarkData: BookmarkData): PlatformFile

public fun PlatformFile.Companion.fromBookmarkData(bytes: ByteArray): PlatformFile =
    fromBookmarkData(BookmarkData(bytes))
