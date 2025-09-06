package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.toByteArray
import io.github.vinceglb.filekit.utils.toKotlinxPath
import io.github.vinceglb.filekit.utils.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSError
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
        return nsUrl.path.hashCode()
    }

    public actual companion object
}

public actual fun PlatformFile(path: Path): PlatformFile =
    if (path.isAbsolute) {
        PlatformFile(NSURL.fileURLWithPath(path = path.toString()))
    } else {
        PlatformFile(NSURL(string = path.toString()))
    }

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

@OptIn(ExperimentalForeignApi::class)
public actual suspend fun PlatformFile.bookmarkData(): BookmarkData = withContext(Dispatchers.IO) {
    withScopedAccess {
        val bookmarkData = nsUrl.bookmarkDataWithOptions(
            options = 0u,
            includingResourceValuesForKeys = null,
            relativeToURL = null,
            error = null
        ) ?: throw FileKitException("Failed to create bookmark data")
        BookmarkData(bookmarkData.toByteArray())
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData
): PlatformFile = memScoped {
    val nsData = bookmarkData.bytes.toNSData()
    val error: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr

    val restoredUrl = NSURL.URLByResolvingBookmarkData(
        bookmarkData = nsData,
        options = 0u,
        relativeToURL = null,
        bookmarkDataIsStale = null,
        error = error
    ) ?: throw FileKitException("Failed to resolve bookmark data: ${error.pointed.value}")

    PlatformFile(restoredUrl)
}