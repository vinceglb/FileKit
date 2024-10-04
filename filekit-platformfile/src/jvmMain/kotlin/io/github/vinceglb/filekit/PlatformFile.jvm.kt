package io.github.vinceglb.filekit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files

public actual data class PlatformFile(
    val file: File,
)

public actual val PlatformFile.underlyingFile: Any
    get() = file

public actual val PlatformFile.name: String
    get() = file.name

public actual val PlatformFile.size: Long
    get() = Files.size(file.toPath())

public actual val PlatformFile.path: String
    get() = file.absolutePath

public actual suspend fun PlatformFile.readBytes(): ByteArray =
    withContext(Dispatchers.IO) { file.readBytes() }

public actual fun PlatformFile.getStream(): PlatformInputStream {
    return io.github.vinceglb.filekit.PlatformInputStream(file.inputStream())
}
