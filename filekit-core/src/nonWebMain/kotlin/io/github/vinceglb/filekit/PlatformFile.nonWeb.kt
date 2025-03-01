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

public expect fun PlatformFile(path: Path): PlatformFile

public fun PlatformFile(path: String): PlatformFile = PlatformFile(Path(path))

public expect fun PlatformFile.toPath(): Path

public expect fun PlatformFile.parent(): PlatformFile?

public expect fun PlatformFile.absolutePath(): PlatformFile

public expect fun PlatformFile.source(): RawSource

public expect fun PlatformFile.sink(append: Boolean = false): RawSink

public fun PlatformFile(base: PlatformFile, child: String): PlatformFile =
    PlatformFile(base.toPath() / child)

public expect fun PlatformFile.isRegularFile(): Boolean

public expect fun PlatformFile.isDirectory(): Boolean

public expect fun PlatformFile.exists(): Boolean

public actual suspend fun PlatformFile.readBytes(): ByteArray =
    withContext(Dispatchers.IO) {
        this@readBytes
            .source()
            .buffered()
            .readByteArray()
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

public suspend infix fun PlatformFile.copyTo(destination: PlatformFile): Unit =
    destination write this

public suspend fun PlatformFile.delete(mustExist: Boolean = true): Unit =
    withContext(Dispatchers.IO) {
        SystemFileSystem.delete(path = toPath(), mustExist = mustExist)
    }

public operator fun PlatformFile.div(child: String): PlatformFile =
    PlatformFile(this, child)

public fun PlatformFile.resolve(relative: String): PlatformFile =
    this / relative
