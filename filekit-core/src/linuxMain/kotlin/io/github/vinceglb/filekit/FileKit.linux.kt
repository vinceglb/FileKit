package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.runSuspendCatchingFileKit
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import platform.posix.getenv
import platform.posix.getpwuid
import platform.posix.getuid

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
            ?: (xdgBaseDirectory(envKey = "XDG_DATA_HOME", homeRelativeFallback = ".local/share") / FileKit.appId)
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.cacheDir: PlatformFile
    get() {
        val folder = FileKit.customCacheDir
            ?: (xdgBaseDirectory(envKey = "XDG_CACHE_HOME", homeRelativeFallback = ".cache") / FileKit.appId)
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.databasesDir: PlatformFile
    get() = FileKit.filesDir / "databases"

public actual val FileKit.projectDir: PlatformFile
    get() = PlatformFile(".")

internal actual fun FileKit.platformUserDirectoryOrNull(type: FileKitUserDirectory): PlatformFile? {
    val home = homeDirectoryOrNull() ?: return null
    val path = resolveLinuxUserDirectoryPath(
        type = type,
        home = home,
        envProvider = ::getEnv,
        linuxUserDirsConfigProvider = { readXdgUserDirsConfig(home) },
    ) ?: return null
    path.assertExists()
    return PlatformFile(path)
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
    throw FileKitException("Image compression is not supported on Linux native target")

@OptIn(ExperimentalForeignApi::class)
private fun getEnv(key: String): String? =
    getenv(key)?.toKString()

private fun xdgBaseDirectory(
    envKey: String,
    homeRelativeFallback: String,
): Path {
    // The XDG Base Directory spec requires the variable to be ignored unless it holds an absolute path
    getEnv(envKey)
        ?.takeIf { it.startsWith("/") }
        ?.let { return Path(it) }

    val home = homeDirectoryOrNull() ?: throw FileKitException(
        "Could not resolve the home directory. Set HOME or call FileKit.init(appId, filesDir, cacheDir) " +
            "with explicit directories.",
    )
    return Path(home, homeRelativeFallback)
}

/**
 * Resolves the user's home directory from `HOME`, falling back to the passwd database so that
 * daemons and containers with an unset environment still get an absolute path.
 */
@OptIn(ExperimentalForeignApi::class)
private fun homeDirectoryOrNull(): String? {
    getEnv("HOME")
        ?.takeIf { it.startsWith("/") }
        ?.let { return it }

    return getpwuid(getuid())
        ?.pointed
        ?.pw_dir
        ?.toKString()
        ?.takeIf { it.startsWith("/") }
}

private operator fun Path.div(child: String): Path = Path(this, child)

private fun Path.assertExists() {
    if (!SystemFileSystem.exists(this)) {
        SystemFileSystem.createDirectories(this)
    }
}

private fun readXdgUserDirsConfig(home: String): String? {
    val configHome = getEnv("XDG_CONFIG_HOME")?.takeIf { it.startsWith("/") } ?: "$home/.config"
    val configFile = Path(configHome, "user-dirs.dirs")
    if (!SystemFileSystem.exists(configFile)) return null

    return runCatching {
        SystemFileSystem.source(configFile).buffered().use { it.readString() }
    }.getOrNull()
}
