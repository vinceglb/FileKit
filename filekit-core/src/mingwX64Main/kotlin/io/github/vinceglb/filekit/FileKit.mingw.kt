@file:Suppress("UnusedReceiverParameter")

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.runSuspendCatchingFileKit
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.getenv
import platform.windows.CoTaskMemFree
import platform.windows.FOLDERID_Documents
import platform.windows.FOLDERID_Downloads
import platform.windows.FOLDERID_Music
import platform.windows.FOLDERID_Pictures
import platform.windows.FOLDERID_Videos
import platform.windows.KNOWNFOLDERID
import platform.windows.SHGetKnownFolderPath

public actual object FileKit {
    private var _appId: String? = null
    internal var customCacheDir: Path? = null
    internal var customFilesDir: Path? = null

    public val appId: String
        get() = _appId
            ?: throw FileKitException("FileKit not initialized. Please call FileKit.init(appId) first.")

    public fun init(appId: String) {
        _appId = appId
        customCacheDir = null
        customFilesDir = null
    }

    public fun init(
        filesDir: PlatformFile,
        cacheDir: PlatformFile,
    ) {
        _appId = null
        customCacheDir = cacheDir.toKotlinxIoPath()
        customFilesDir = filesDir.toKotlinxIoPath()
    }

    public fun init(
        appId: String,
        filesDir: PlatformFile? = null,
        cacheDir: PlatformFile? = null,
    ) {
        _appId = appId
        customCacheDir = cacheDir?.toKotlinxIoPath()
        customFilesDir = filesDir?.toKotlinxIoPath()
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() {
        val folder = FileKit.customFilesDir
            ?: (getEnv("APPDATA") / appId)
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.cacheDir: PlatformFile
    get() {
        val folder = FileKit.customCacheDir
            ?: (getEnv("LOCALAPPDATA") / appId / "Cache")
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.databasesDir: PlatformFile
    get() = FileKit.filesDir / "databases"

public actual val FileKit.projectDir: PlatformFile
    get() = PlatformFile(".")

@OptIn(ExperimentalForeignApi::class)
internal actual fun FileKit.platformUserDirectoryOrNull(type: FileKitUserDirectory): PlatformFile? {
    val knownFolderId = when (type) {
        FileKitUserDirectory.Downloads -> FOLDERID_Downloads
        FileKitUserDirectory.Pictures -> FOLDERID_Pictures
        FileKitUserDirectory.Videos -> FOLDERID_Videos
        FileKitUserDirectory.Music -> FOLDERID_Music
        FileKitUserDirectory.Documents -> FOLDERID_Documents
    }

    // Primary: SHGetKnownFolderPath
    val path = resolveKnownFolder(knownFolderId)
    if (path != null) {
        path.assertExists()
        return PlatformFile(path)
    }

    // Fallback: USERPROFILE + default folder name (same as JVM)
    val userProfile = getenv("USERPROFILE")?.toKString() ?: return null
    val fallbackDir = when (type) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Videos"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }
    val fallbackPath = Path(userProfile) / fallbackDir
    fallbackPath.assertExists()
    return PlatformFile(fallbackPath)
}

public actual suspend fun FileKit.saveImageToGallery(
    bytes: ByteArray,
    filename: String,
): Result<Unit> = runSuspendCatchingFileKit {
    FileKit.picturesDir / filename write bytes
}

public actual suspend fun FileKit.saveVideoToGallery(
    file: PlatformFile,
    filename: String,
): Result<Unit> = runSuspendCatchingFileKit {
    FileKit.videosDir / filename write file
}

public actual suspend fun FileKit.compressImage(
    bytes: ByteArray,
    imageFormat: ImageFormat,
    @androidx.annotation.IntRange(from = 0, to = 100) quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
): ByteArray =
    throw FileKitException("Image compression is not supported on Windows native target")

@OptIn(ExperimentalForeignApi::class)
private fun getEnv(key: String): Path {
    val value = getenv(key)?.toKString()
        ?: throw IllegalStateException("Environment variable $key not found.")
    return Path(value)
}

private operator fun Path.div(child: String): Path = Path(this, child)

private fun Path.assertExists() {
    if (!SystemFileSystem.exists(this)) {
        SystemFileSystem.createDirectories(this)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun resolveKnownFolder(folderId: KNOWNFOLDERID): Path? = memScoped {
    val ppszPath = alloc<CPointerVarOf<kotlinx.cinterop.CPointer<UShortVar>>>()
    val hr = SHGetKnownFolderPath(
        rfid = folderId.ptr,
        dwFlags = 0u,
        hToken = null,
        ppszPath = ppszPath.ptr,
    )
    if (hr == 0) {
        val pathStr = ppszPath.value?.toKStringFromUtf16()
        CoTaskMemFree(ppszPath.value)
        pathStr?.let { Path(it) }
    } else {
        null
    }
}
