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

public expect fun PlatformFile(path: Path): PlatformFile

public fun PlatformFile(path: String): PlatformFile = PlatformFile(Path(path))

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

public actual suspend fun PlatformFile.readBytes(): ByteArray =
    withContext(Dispatchers.IO) {
        this@readBytes
            .source()
            .buffered()
            .readByteArray()
    }

public actual suspend fun PlatformFile.readString(): String =
    withContext(Dispatchers.IO) {
        this@readString
            .source()
            .buffered()
            .readString()
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
        val source = platformFile.source()
        val size = platformFile.size()
        this@write
            .sink()
            .buffered()
            .use { it.write(source, size) }
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

public suspend fun PlatformFile.createDirectories(mustCreate: Boolean = false): Unit =
    withContext(Dispatchers.IO) {
        SystemFileSystem.createDirectories(toKotlinxIoPath(), mustCreate)
    }

public inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
        block(directoryFiles)
    }

public fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem.list(toKotlinxIoPath()).map(::PlatformFile)
    }

public fun PlatformFile.atomicMove(destination: PlatformFile): Unit =
    withScopedAccess {
        SystemFileSystem.atomicMove(
            source = toKotlinxIoPath(),
            destination = destination.toKotlinxIoPath(),
        )
    }

public suspend fun PlatformFile.delete(mustExist: Boolean = true): Unit =
    withContext(Dispatchers.IO) {
        SystemFileSystem.delete(path = toKotlinxIoPath(), mustExist = mustExist)
    }

public operator fun PlatformFile.div(child: String): PlatformFile =
    PlatformFile(this, child)

public fun PlatformFile.resolve(relative: String): PlatformFile =
    this / relative

public expect fun PlatformFile.startAccessingSecurityScopedResource(): Boolean

public expect fun PlatformFile.stopAccessingSecurityScopedResource()

public inline fun <T> PlatformFile.withScopedAccess(block: (PlatformFile) -> T): T = try {
    startAccessingSecurityScopedResource()
    block(this)
} finally {
    stopAccessingSecurityScopedResource()
}
