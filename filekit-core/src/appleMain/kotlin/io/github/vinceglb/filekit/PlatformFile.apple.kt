package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSURL

public actual data class PlatformFile(
    val nsUrl: NSURL,
) {
    public actual override fun toString(): String = path

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformFile) return false
        if (nsUrl.path != other.nsUrl.path) return false
        return true
    }

    override fun hashCode(): Int {
        return nsUrl.hashCode()
    }
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL(string = path.toString()))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    nsUrl.toKotlinxPath()

public actual val PlatformFile.extension: String
    get() = nsUrl.pathExtension ?: ""

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".", name)

public actual fun PlatformFile.absolutePath(): String =
    nsUrl.absoluteString ?: ""

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(NSURL.fileURLWithPath(it.toString())) }
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(NSURL.fileURLWithPath(it.toString())) }
    }

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean =
    nsUrl.startAccessingSecurityScopedResource()

public actual fun PlatformFile.stopAccessingSecurityScopedResource(): Unit =
    nsUrl.stopAccessingSecurityScopedResource()
