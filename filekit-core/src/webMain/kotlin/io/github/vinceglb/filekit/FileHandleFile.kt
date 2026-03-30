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
    public val file: FileExt,
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

    override fun getFile(): FileExt = file

    override fun getParent(): WebFileHandle? =
        parent

    override fun list(): List<WebFileHandle> =
        emptyList()
}

public fun PlatformFile(file: FileExt): PlatformFile = PlatformFile(fh = FileHandleFile(file))
