package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.utils.runSuspendCatchingFileKit
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.readString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.cinterop.toKString
import platform.posix.getenv

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
            ?: (getEnv("XDG_DATA_HOME")?.let { Path(it) } ?: (getEnv("HOME")?.let { Path(it, ".local/share") } ?: Path(".local/share"))) / FileKit.appId
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.cacheDir: PlatformFile
    get() {
        val folder = FileKit.customCacheDir
            ?: (getEnv("XDG_CACHE_HOME")?.let { Path(it) } ?: (getEnv("HOME")?.let { Path(it, ".cache") } ?: Path(".cache"))) / FileKit.appId
        folder.assertExists()
        return PlatformFile(folder)
    }

public actual val FileKit.databasesDir: PlatformFile
    get() = FileKit.filesDir / "databases"

public actual val FileKit.projectDir: PlatformFile
    get() = PlatformFile(".")

@OptIn(ExperimentalForeignApi::class)
internal actual fun FileKit.platformUserDirectoryOrNull(type: FileKitUserDirectory): PlatformFile? {
    val home = getEnv("HOME") ?: return null

    val envKey = when (type) {
        FileKitUserDirectory.Downloads -> "XDG_DOWNLOAD_DIR"
        FileKitUserDirectory.Pictures -> "XDG_PICTURES_DIR"
        FileKitUserDirectory.Videos -> "XDG_VIDEOS_DIR"
        FileKitUserDirectory.Music -> "XDG_MUSIC_DIR"
        FileKitUserDirectory.Documents -> "XDG_DOCUMENTS_DIR"
    }

    val fallbackName = when (type) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Videos"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }

    val envValue = getEnv(envKey)?.takeIf(String::isNotBlank)?.let { expandHomeVariable(it, home) }
    if (envValue != null) {
        val path = Path(envValue)
        path.assertExists()
        return PlatformFile(path)
    }

    // Try reading ~/.config/user-dirs.dirs for the actual config (simplistic parsing)
    val configHome = getEnv("XDG_CONFIG_HOME")?.takeIf(String::isNotBlank) ?: "$home/.config"
    val configFile = Path(configHome, "user-dirs.dirs")
    if (SystemFileSystem.exists(configFile)) {
        val content = runCatching { SystemFileSystem.source(configFile).buffered().use { it.readString() } }.getOrNull()
        if (content != null) {
            val configuredValue = parseXdgUserDirsConfig(content)[envKey]?.takeIf(String::isNotBlank)?.let { expandHomeVariable(it, home) }
            if (configuredValue != null) {
                val path = Path(configuredValue)
                path.assertExists()
                return PlatformFile(path)
            }
        }
    }

    val fallbackPath = Path(home, fallbackName)
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
    throw FileKitException("Image compression is not supported on Linux native target")

@OptIn(ExperimentalForeignApi::class)
private fun getEnv(key: String): String? =
    getenv(key)?.toKString()

private operator fun Path.div(child: String): Path = Path(this, child)

private fun Path.assertExists() {
    if (!SystemFileSystem.exists(this)) {
        SystemFileSystem.createDirectories(this)
    }
}

private fun expandHomeVariable(path: String, home: String): String =
    path
        .replace("\${HOME}", home)
        .replace("\$HOME", home)

private fun parseXdgUserDirsConfig(config: String): Map<String, String> =
    buildMap {
        config
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .filterNot { it.startsWith("#") }
            .forEach { line ->
                val key = line.substringBefore("=", missingDelimiterValue = "").trim()
                val rawValue = line.substringAfter("=", missingDelimiterValue = "").trim()

                if (key.isBlank() || rawValue.isBlank()) {
                    return@forEach
                }

                val value = rawValue
                    .removeSurrounding("\"")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")

                put(key, value)
            }
    }


