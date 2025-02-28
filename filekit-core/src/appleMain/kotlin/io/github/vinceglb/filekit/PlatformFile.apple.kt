package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSURL

public actual data class PlatformFile(
    val nsUrl: NSURL,
)

// Constructors

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL.fileURLWithPath(path.toString()))

// Extension Properties

public actual fun PlatformFile.toPath(): Path =
    nsUrl.toKotlinxPath()

public actual val PlatformFile.name: String
    get() = toPath().name

public actual val PlatformFile.extension: String
    get() = nsUrl.pathExtension ?: ""

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

public actual fun PlatformFile.isRegularFile(): Boolean =
    SystemFileSystem.metadataOrNull(toPath())?.isRegularFile ?: false

public actual fun PlatformFile.isDirectory(): Boolean =
    SystemFileSystem.metadataOrNull(toPath())?.isDirectory ?: false

public actual fun PlatformFile.exists(): Boolean =
    SystemFileSystem.exists(toPath())

public actual fun PlatformFile.size(): Long =
    SystemFileSystem.metadataOrNull(toPath())?.size ?: -1

public actual fun PlatformFile.parent(): PlatformFile? =
    toPath().parent?.let(::PlatformFile)

public actual fun PlatformFile.absolutePath(): PlatformFile =
    PlatformFile(SystemFileSystem.resolve(toPath()))

// IO Operations with kotlinx-io

public actual fun PlatformFile.source(): RawSource = try {
    nsUrl.startAccessingSecurityScopedResource()
    SystemFileSystem.source(toPath())
} finally {
    nsUrl.stopAccessingSecurityScopedResource()
}

public actual fun PlatformFile.sink(append: Boolean): RawSink =
    SystemFileSystem.sink(toPath(), append)
