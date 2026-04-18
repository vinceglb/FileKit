package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import platform.windows.DWORDVar
import platform.windows.FILETIME
import platform.windows.GET_FILEEX_INFO_LEVELS
import platform.windows.GetFileAttributesExW
import platform.windows.GetFullPathNameW
import platform.windows.HKEYVar
import platform.windows.HKEY_CLASSES_ROOT
import platform.windows.KEY_READ
import platform.windows.MAX_PATH
import platform.windows.RegCloseKey
import platform.windows.RegOpenKeyExW
import platform.windows.RegQueryValueExW
import platform.windows.WIN32_FILE_ATTRIBUTE_DATA
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Wrapper for a file path on Windows native platform.
 */
public class WindowsPath(
    public val path: Path,
)

/**
 * Represents a file on the Windows native platform.
 *
 * @property windowsPath The underlying wrapped [Path] object.
 */
@Serializable(with = PlatformFileSerializer::class)
public actual class PlatformFile(
    public val windowsPath: WindowsPath,
) {
    public actual override fun toString(): String = windowsPath.path.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlatformFile) return false
        return windowsPath.path.toString() == other.windowsPath.path.toString()
    }

    override fun hashCode(): Int = windowsPath.path.toString().hashCode()

    public actual companion object
}

public actual fun PlatformFile(path: Path): PlatformFile =
    PlatformFile(windowsPath = WindowsPath(path))

public actual fun PlatformFile.toKotlinxIoPath(): Path =
    windowsPath.path

public actual val PlatformFile.extension: String
    get() = name.substringAfterLast('.', "")

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast('.', name)

@OptIn(ExperimentalForeignApi::class)
public actual fun PlatformFile.absolutePath(): String {
    val rawPath = windowsPath.path.toString()
    // Already absolute (drive letter or UNC path)
    if (rawPath.isDriveAbsolutePath()) return rawPath
    if (rawPath.startsWith("\\\\")) return rawPath

    // Resolve relative path using Win32 API (Unicode-safe)
    return resolveFullPath(rawPath) ?: rawPath
}

private fun String.isDriveAbsolutePath(): Boolean =
    length >= 3 &&
        this[1] == ':' &&
        (this[2] == '\\' || this[2] == '/')

@OptIn(ExperimentalForeignApi::class)
private fun resolveFullPath(rawPath: String): String? = memScoped {
    val initialBuffer = allocArray<UShortVar>(MAX_PATH)
    val requiredLength = GetFullPathNameW(rawPath, MAX_PATH.toUInt(), initialBuffer, null)

    when {
        requiredLength == 0u -> {
            null
        }

        requiredLength < MAX_PATH.toUInt() -> {
            initialBuffer.toKStringFromUtf16()
        }

        else -> {
            val fullBuffer = allocArray<UShortVar>(requiredLength.toInt())
            val resolvedLength = GetFullPathNameW(rawPath, requiredLength, fullBuffer, null)
            if (resolvedLength == 0u) null else fullBuffer.toKStringFromUtf16()
        }
    }
}

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit): Unit =
    withScopedAccess {
        val directoryFiles = SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(it) }
        block(directoryFiles)
    }

public actual fun PlatformFile.list(): List<PlatformFile> =
    withScopedAccess {
        SystemFileSystem
            .list(toKotlinxIoPath())
            .map { PlatformFile(it) }
    }

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.createdAt(): Instant? =
    getFileAttributeData()?.ftCreationTime?.toInstant()

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
public actual fun PlatformFile.lastModified(): Instant {
    val data = getFileAttributeData()
        ?: return Instant.fromEpochMilliseconds(0L)
    return data.ftLastWriteTime.toInstant()
}

@OptIn(ExperimentalForeignApi::class)
public actual fun PlatformFile.mimeType(): MimeType? {
    val ext = extension.lowercase()
    if (ext.isBlank()) return null
    return mimeTypeFromRegistry(".$ext")
}

@OptIn(ExperimentalForeignApi::class)
private fun mimeTypeFromRegistry(dotExtension: String): MimeType? = memScoped {
    val hKey = alloc<HKEYVar>()
    val result = RegOpenKeyExW(
        HKEY_CLASSES_ROOT,
        dotExtension,
        0u,
        KEY_READ.toUInt(),
        hKey.ptr,
    )
    if (result != 0) return null

    try {
        val bufferSize = alloc<DWORDVar>()
        bufferSize.value = 512u
        val buffer = allocArray<ByteVar>(512)

        val queryResult = RegQueryValueExW(
            hKey.value,
            "Content Type",
            null,
            null,
            buffer.reinterpret(),
            bufferSize.ptr,
        )
        if (queryResult != 0) return null

        val mimeString = buffer.reinterpret<UShortVar>().toKStringFromUtf16()
        if (mimeString.isBlank()) return null
        MimeType.parse(mimeString)
    } finally {
        RegCloseKey(hKey.value)
    }
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() {}

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData =
    withContext(Dispatchers.IO) {
        BookmarkData(absolutePath().encodeToByteArray())
    }

public actual fun PlatformFile.releaseBookmark() {}

public actual fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile {
    val restoredPath = bookmarkData.bytes.decodeToString()
    return PlatformFile(windowsPath = WindowsPath(Path(restoredPath)))
}

@OptIn(ExperimentalForeignApi::class)
private fun PlatformFile.getFileAttributeData(): WIN32_FILE_ATTRIBUTE_DATA? = memScoped {
    val data = alloc<WIN32_FILE_ATTRIBUTE_DATA>()
    val filePath = absolutePath()
    val success = GetFileAttributesExW(
        filePath,
        GET_FILEEX_INFO_LEVELS.GetFileExInfoStandard,
        data.ptr,
    )
    if (success != 0) data else null
}

@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
private fun FILETIME.toInstant(): Instant {
    // Windows FILETIME: 100-nanosecond intervals since January 1, 1601
    // Unix epoch: January 1, 1970
    // Difference: 11644473600 seconds
    val windowsTicks = dwHighDateTime.toLong().shl(32) or dwLowDateTime.toLong()
    val epochMillis = (windowsTicks / 10_000L) - 11_644_473_600_000L
    return Instant.fromEpochMilliseconds(epochMillis)
}
