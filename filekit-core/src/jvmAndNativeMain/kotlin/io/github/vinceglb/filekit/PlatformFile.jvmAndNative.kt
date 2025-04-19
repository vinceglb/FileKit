package io.github.vinceglb.filekit

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.SystemFileSystem

public actual val PlatformFile.name: String
    get() = toKotlinxIoPath().name

public actual val PlatformFile.path: String
    get() = toKotlinxIoPath().toString()

public actual fun PlatformFile.isRegularFile(): Boolean = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isRegularFile ?: false
}

public actual fun PlatformFile.isDirectory(): Boolean = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isDirectory ?: false
}

public actual fun PlatformFile.isAbsolute(): Boolean =
    toKotlinxIoPath().isAbsolute

public actual fun PlatformFile.exists(): Boolean =
    SystemFileSystem.exists(toKotlinxIoPath())

public actual fun PlatformFile.size(): Long = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.size ?: -1
}

public actual fun PlatformFile.parent(): PlatformFile? =
    toKotlinxIoPath().parent?.let(::PlatformFile)

public actual fun PlatformFile.absoluteFile(): PlatformFile = withScopedAccess {
    PlatformFile(SystemFileSystem.resolve(toKotlinxIoPath()))
}

public actual fun PlatformFile.source(): RawSource = withScopedAccess {
    SystemFileSystem.source(toKotlinxIoPath())
}

public actual fun PlatformFile.sink(append: Boolean): RawSink = withScopedAccess {
    SystemFileSystem.sink(toKotlinxIoPath(), append)
}
