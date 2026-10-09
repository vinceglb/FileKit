package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toDouble
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

public actual suspend fun PlatformFile.listAsync(): List<PlatformFile> = when (val file = webFile) {
    is WebFile.FileWrapper,
    is WebFile.OriginPrivateFile,
    -> throw FileKitException("Cannot list a regular file")

    is WebFile.DirectoryWrapper -> file.children.map { it.toPlatformFile() }

    is WebFile.OriginPrivateDirectory -> file.list()
}

public actual suspend fun PlatformFile.file(
    name: String,
    create: Boolean,
): PlatformFile = when (webFile) {
    is WebFile.OriginPrivateDirectory -> webFile.file(name, create)
    else -> throw FileKitException("This file is not a writable origin private file system directory")
}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.directory(
    name: String,
    create: Boolean,
): PlatformFile = when (webFile) {
    is WebFile.OriginPrivateDirectory -> {
        webFile.directory(name, create)
    }

    else -> {
        throw FileKitException("This file is not a writable origin private file system directory")
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.existsAsync(): Boolean = when (val file = webFile) {
    is WebFile.FileWrapper,
    is WebFile.DirectoryWrapper,
    -> true

    is WebFile.OriginPrivateFile -> file.parent?.let { parent ->
        runCatching { parent.handle.getFileHandle(file.name, FileSystemGetHandleOptions(false)).await() }.isSuccess
    } ?: true

    is WebFile.OriginPrivateDirectory -> file.parent?.let { parent ->
        runCatching { parent.handle.getDirectoryHandle(file.name, FileSystemGetHandleOptions(false)).await() }.isSuccess
    } ?: true
}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.sizeAsync(): Long = when (val file = webFile) {
    is WebFile.FileWrapper -> {
        file.size
    }

    is WebFile.DirectoryWrapper,
    is WebFile.OriginPrivateDirectory,
    -> {
        0
    }

    is WebFile.OriginPrivateFile -> {
        file.handle
            .getFile()
            .await()
            .size
            .toDouble()
            .toLong()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.mimeTypeAsync(): MimeType? = when (val file = webFile) {
    is WebFile.FileWrapper -> {
        file.type.takeIf(String::isNotBlank)?.let(MimeType::parse)
    }

    is WebFile.DirectoryWrapper,
    is WebFile.OriginPrivateDirectory,
    -> {
        null
    }

    is WebFile.OriginPrivateFile -> {
        file.handle
            .getFile()
            .await()
            .type
            .takeIf(String::isNotBlank)
            ?.let(MimeType::parse)
    }
}

@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
@OptIn(ExperimentalTime::class, ExperimentalWasmJsInterop::class)
public actual suspend fun PlatformFile.lastModifiedAsync(): Instant = when (val file = webFile) {
    is WebFile.FileWrapper -> file.lastModified

    is WebFile.DirectoryWrapper,
    is WebFile.OriginPrivateDirectory,
    -> WEB_DIRECTORY_LAST_MODIFIED

    is WebFile.OriginPrivateFile -> Instant.fromEpochMilliseconds(
        file.handle
            .getFile()
            .await()
            .lastModified
            .toDouble()
            .toLong(),
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
internal actual suspend fun PlatformFile.updatePlatformData() {
    when (val file = webFile) {
        is WebFile.OriginPrivateFile -> file.updateSnapshot(file.handle.getFile().await())
        else -> Unit
    }
}
