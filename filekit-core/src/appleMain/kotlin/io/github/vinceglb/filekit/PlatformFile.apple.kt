package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.files.Path
import platform.Foundation.NSURL

public actual data class PlatformFile(
    val nsUrl: NSURL,
) {
    public actual override fun toString(): String = path
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL.fileURLWithPath(path = path.toString()))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    nsUrl.toKotlinxPath()

public actual val PlatformFile.extension: String
    get() = nsUrl.pathExtension ?: ""

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

public actual fun PlatformFile.absolutePath(): String =
    nsUrl.absoluteString ?: ""

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean =
    nsUrl.startAccessingSecurityScopedResource()

public actual fun PlatformFile.stopAccessingSecurityScopedResource(): Unit =
    nsUrl.stopAccessingSecurityScopedResource()
