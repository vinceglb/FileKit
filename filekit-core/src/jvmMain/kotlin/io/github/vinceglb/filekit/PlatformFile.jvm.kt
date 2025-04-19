package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toKotlinxIoPath
import kotlinx.io.files.Path
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

public actual val PlatformFile.extension: String
    get() = file.extension

public actual val PlatformFile.nameWithoutExtension: String
    get() = file.nameWithoutExtension

public actual fun PlatformFile.absolutePath(): String =
    file.absolutePath

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}
