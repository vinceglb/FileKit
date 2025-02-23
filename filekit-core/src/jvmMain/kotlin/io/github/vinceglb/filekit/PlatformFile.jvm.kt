package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File
import java.nio.file.Files

public actual data class PlatformFile(
    val file: File,
)

// Constructors

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(path.toFile())

// Extension Properties

public actual val PlatformFile.path: Path
    get() = file.toKotlinxPath()

public actual val PlatformFile.name: String
    get() = path.name

public actual val PlatformFile.isFile: Boolean
    get() = file.isFile

public actual val PlatformFile.isDirectory: Boolean
    get() = file.isDirectory

public actual val PlatformFile.exists: Boolean
    get() = file.exists()

public actual val PlatformFile.size: Long
    get() = Files.size(file.toPath())

public actual val PlatformFile.parent: PlatformFile?
    get() = file.parentFile?.let { PlatformFile(it) }

public actual val PlatformFile.absolutePath: String
    get() = file.absolutePath

// IO Operations with kotlinx-io

public actual fun PlatformFile.source(): RawSource? =
    path.let { SystemFileSystem.source(path = it) }

public actual fun PlatformFile.sink(append: Boolean): RawSink? =
    path.let { SystemFileSystem.sink(path = it, append = append) }
