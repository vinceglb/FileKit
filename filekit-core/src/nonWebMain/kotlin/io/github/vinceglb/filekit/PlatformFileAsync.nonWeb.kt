package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

public actual suspend fun PlatformFile.listAsync(): List<PlatformFile> = list()

public actual suspend fun PlatformFile.file(
    name: String,
    create: Boolean,
): PlatformFile {
    val file = this / name
    if (create && !file.exists()) {
        file write ByteArray(0)
    }
    if (!file.exists() || !file.isRegularFile()) {
        throw FileKitException("File does not exist: $file")
    }
    return file
}

public actual suspend fun PlatformFile.directory(
    name: String,
    create: Boolean,
): PlatformFile {
    val directory = this / name
    if (create) {
        directory.createDirectories()
    } else if (!directory.exists() || !directory.isDirectory()) {
        throw FileKitException("Directory does not exist: $directory")
    }
    return directory
}

public actual suspend fun PlatformFile.existsAsync(): Boolean = exists()

public actual suspend fun PlatformFile.sizeAsync(): Long = size()

public actual suspend fun PlatformFile.mimeTypeAsync(): MimeType? = mimeType()

@OptIn(ExperimentalTime::class)
public actual suspend fun PlatformFile.lastModifiedAsync(): Instant = lastModified()

internal actual suspend fun PlatformFile.updatePlatformData() {}
