package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

public actual val PlatformFile.name: String
    get() = toKotlinxIoPath().name

public actual val PlatformFile.path: String
    get() = toKotlinxIoPath().toString()

public actual fun PlatformFile(path: String): PlatformFile =
    PlatformFile(Path(path))

public actual fun PlatformFile(base: PlatformFile, child: String): PlatformFile =
    base.withPath(base.toKotlinxIoPath() / child)

public actual fun PlatformFile.isRegularFile(): Boolean = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isRegularFile ?: false
}

public actual fun PlatformFile.isDirectory(): Boolean = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.isDirectory ?: false
}

public actual fun PlatformFile.isAbsolute(): Boolean =
    toKotlinxIoPath().isAbsolute

public actual fun PlatformFile.exists(): Boolean = withScopedAccess {
    SystemFileSystem.exists(toKotlinxIoPath())
}

public actual fun PlatformFile.size(): Long = withScopedAccess {
    SystemFileSystem.metadataOrNull(toKotlinxIoPath())?.size ?: -1
}

public actual fun PlatformFile.parent(): PlatformFile? =
    toKotlinxIoPath().parent?.let(::withPath)

public actual fun PlatformFile.absoluteFile(): PlatformFile = withScopedAccess {
    withPath(SystemFileSystem.resolve(toKotlinxIoPath()))
}

public actual fun PlatformFile.source(): RawSource {
    val accessGranted = startAccessingSecurityScopedResource()
    try {
        return ScopedRawSource(
            delegate = SystemFileSystem.source(toKotlinxIoPath()),
            onClose = { if (accessGranted) stopAccessingSecurityScopedResource() },
        )
    } catch (error: Throwable) {
        if (accessGranted) stopAccessingSecurityScopedResource()
        throw error
    }
}

public actual fun PlatformFile.sink(append: Boolean): RawSink {
    val accessGranted = startAccessingSecurityScopedResource()
    try {
        return ScopedRawSink(
            delegate = SystemFileSystem.sink(toKotlinxIoPath(), append),
            onClose = { if (accessGranted) stopAccessingSecurityScopedResource() },
        )
    } catch (error: Throwable) {
        if (accessGranted) stopAccessingSecurityScopedResource()
        throw error
    }
}

public actual fun PlatformFile.createDirectories(mustCreate: Boolean): Unit =
    withScopedAccess {
        SystemFileSystem.createDirectories(toKotlinxIoPath(), mustCreate)
    }

public actual suspend fun PlatformFile.delete(mustExist: Boolean): Unit =
    withScopedAccess {
        withContext(Dispatchers.IO) {
            SystemFileSystem.delete(path = toKotlinxIoPath(), mustExist = mustExist)
        }
    }

public actual suspend fun PlatformFile.atomicMove(destination: PlatformFile): Unit =
    withContext(Dispatchers.IO) {
        withScopedAccess {
            destination.withScopedAccess { scopedDestination ->
                val resolvedDestination = scopedDestination.resolveAtomicMoveDestination(
                    source = this@atomicMove,
                )
                SystemFileSystem.atomicMove(
                    source = toKotlinxIoPath(),
                    destination = resolvedDestination.toKotlinxIoPath(),
                )
            }
        }
    }

private fun PlatformFile.resolveAtomicMoveDestination(source: PlatformFile): PlatformFile {
    if (isDirectory()) {
        return withPath(toKotlinxIoPath() / source.name)
    }
    return this
}

internal actual suspend fun PlatformFile.prepareDestinationForWrite(
    source: PlatformFile,
): PlatformFile = withScopedAccess {
    if (isDirectory()) {
        withPath(toKotlinxIoPath() / source.name)
    } else {
        this
    }
}

@PublishedApi
internal expect fun PlatformFile.withPath(path: Path): PlatformFile

private class ScopedRawSource(
    private val delegate: RawSource,
    private val onClose: () -> Unit,
) : RawSource {
    private var closed = false

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long = delegate.readAtMostTo(sink, byteCount)

    override fun close() {
        if (closed) return
        closed = true
        try {
            delegate.close()
        } finally {
            onClose()
        }
    }
}

private class ScopedRawSink(
    private val delegate: RawSink,
    private val onClose: () -> Unit,
) : RawSink {
    private var closed = false

    override fun write(source: Buffer, byteCount: Long) {
        delegate.write(source, byteCount)
    }

    override fun flush() {
        delegate.flush()
    }

    override fun close() {
        if (closed) return
        closed = true
        try {
            delegate.close()
        } finally {
            onClose()
        }
    }
}
