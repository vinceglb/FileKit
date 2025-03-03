package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toKotlinxIoPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

public actual data class PlatformFile(
    val file: File,
) {
    public actual override fun toString(): String = path
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(path.toFile())

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    file.toKotlinxIoPath()

public actual val PlatformFile.name: String
    get() = toKotlinxIoPath().name

public actual val PlatformFile.extension: String
    get() = file.extension

public actual val PlatformFile.nameWithoutExtension: String
    get() = file.nameWithoutExtension

public actual val PlatformFile.path: String
    get() = toKotlinxIoPath().toString()

public actual fun PlatformFile.isRegularFile(): Boolean =
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isRegularFile ?: false

public actual fun PlatformFile.isDirectory(): Boolean =
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isDirectory ?: false

public actual fun PlatformFile.isAbsolute(): Boolean =
    toKotlinxIoPath().isAbsolute

public actual fun PlatformFile.exists(): Boolean =
    SystemFileSystem.exists(toKotlinxIoPath())

public actual fun PlatformFile.size(): Long =
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.size ?: -1

public actual fun PlatformFile.parent(): PlatformFile? =
    toKotlinxIoPath().parent?.let(::PlatformFile)

public actual fun PlatformFile.absolutePath(): String =
    file.absolutePath

public actual fun PlatformFile.absoluteFile(): PlatformFile =
    PlatformFile(SystemFileSystem.resolve(toKotlinxIoPath()))

public actual fun PlatformFile.source(): RawSource =
    SystemFileSystem.source(toKotlinxIoPath())

public actual fun PlatformFile.sink(append: Boolean): RawSink =
    SystemFileSystem.sink(toKotlinxIoPath(), append)
