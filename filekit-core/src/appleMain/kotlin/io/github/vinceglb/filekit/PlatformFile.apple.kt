package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.toKotlinxPath
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.NSURLIsDirectoryKey
import platform.Foundation.NSURLIsRegularFileKey

public actual data class PlatformFile(
    val nsUrl: NSURL,
)

// Constructors

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(NSURL.fileURLWithPath(path.toString()))

// Extension Properties

public actual val PlatformFile.path: Path?
    get() = nsUrl.toKotlinxPath()

public actual val PlatformFile.name: String?
    get() = path?.name

@OptIn(ExperimentalForeignApi::class)
public actual val PlatformFile.isFile: Boolean
    get() {
        val values = nsUrl.resourceValuesForKeys(listOf(NSURLIsRegularFileKey), null)
        val isFile = values?.get(NSURLIsRegularFileKey) as? Boolean
        return isFile == true
    }

@OptIn(ExperimentalForeignApi::class)
public actual val PlatformFile.isDirectory: Boolean
    get() {
        val values = nsUrl.resourceValuesForKeys(listOf(NSURLIsDirectoryKey), null)
        val isDirectory = values?.get(NSURLIsDirectoryKey) as? Boolean
        return isDirectory == true
    }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public actual val PlatformFile.size: Long?
    get() {
        memScoped {
            val valuePointer: CPointer<ObjCObjectVar<Any?>> = alloc<ObjCObjectVar<Any?>>().ptr
            val errorPointer: CPointer<ObjCObjectVar<NSError?>> =
                alloc<ObjCObjectVar<NSError?>>().ptr
            nsUrl.getResourceValue(valuePointer, NSURLFileSizeKey, errorPointer)
            return valuePointer.pointed.value as Long
        }
    }

public actual val PlatformFile.exists: Boolean
    get() = path?.let { SystemFileSystem.exists(it) } ?: false

public actual val PlatformFile.parent: PlatformFile?
    get() = nsUrl.URLByDeletingLastPathComponent()?.let { PlatformFile(it) }

// IO Operations with kotlinx-io

public actual fun PlatformFile.source(): RawSource? =
    path?.let { SystemFileSystem.source(path = it) }

public actual fun PlatformFile.sink(append: Boolean): RawSink? =
    path?.let { SystemFileSystem.sink(path = it, append = append) }
