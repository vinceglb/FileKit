package io.github.vinceglb.filekit

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toDouble
import kotlin.time.Instant

/**
 * Implementation of WebFileHandle
 * Using just the W3C File object
 */
@OptIn(ExperimentalWasmJsInterop::class)
public class FileHandleFile(
    public val file: File,
    public val parent: WebFileHandle? = null,
) : WebFileHandle {
    override val name: String
        get() = file.name
    override val type: String
        get() = file.type
    override val size: Long
        get() = file.size.toDouble().toLong()
    override val path: String
        get() = file.webkitRelativePath ?: ""
    public override val isDirectory: Boolean = false
    public override val isRegularFile: Boolean = true
    override val lastModified: Instant
        get() = Instant.fromEpochMilliseconds(file.lastModified.toDouble().toLong())

    override fun getFile(): File = file

    override fun getParent(): PlatformFile? =
        parent?.let { PlatformFile(fh = it) }

    override fun list(): List<PlatformFile> =
        emptyList()
}

public fun PlatformFile(file: File): PlatformFile = PlatformFile(fh = FileHandleFile(file))
