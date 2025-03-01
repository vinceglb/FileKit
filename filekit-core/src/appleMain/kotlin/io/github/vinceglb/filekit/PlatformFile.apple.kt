package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSURL

public actual data class PlatformFile(
    val nsUrl: NSURL,
) {
    public actual override fun toString(): String = path
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL(string = path.toString()))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    nsUrl.toKotlinxPath()

public actual val PlatformFile.name: String
    get() = toKotlinxIoPath().name

public actual val PlatformFile.extension: String
    get() = nsUrl.pathExtension ?: ""

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

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

public actual fun PlatformFile.resolve(): PlatformFile =
    PlatformFile(SystemFileSystem.resolve(toKotlinxIoPath()))

public actual fun PlatformFile.absolutePath(): String =
    nsUrl.absoluteString ?: ""

public actual fun PlatformFile.source(): RawSource = try {
    nsUrl.startAccessingSecurityScopedResource()
    SystemFileSystem.source(toKotlinxIoPath())
} finally {
    nsUrl.stopAccessingSecurityScopedResource()
}

public actual fun PlatformFile.sink(append: Boolean): RawSink =
    SystemFileSystem.sink(toKotlinxIoPath(), append)
