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

/**
 * The XDG variable name and default folder name for each user directory, kept together so the two
 * cannot drift apart.
 */
private val FileKitUserDirectory.xdgEnvKey: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "XDG_DOWNLOAD_DIR"
        FileKitUserDirectory.Pictures -> "XDG_PICTURES_DIR"
        FileKitUserDirectory.Videos -> "XDG_VIDEOS_DIR"
        FileKitUserDirectory.Music -> "XDG_MUSIC_DIR"
        FileKitUserDirectory.Documents -> "XDG_DOCUMENTS_DIR"
    }

private val FileKitUserDirectory.defaultFolderName: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Videos"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }

internal actual fun FileKit.platformUserDirectoryOrNull(type: FileKitUserDirectory): PlatformFile? {
    val home = homeDirectoryOrNull() ?: return null
    val envKey = type.xdgEnvKey
    val fallbackName = type.defaultFolderName

    // Primary: the XDG user directory environment variable
    val envValue = getEnv(envKey)
        ?.takeIf(String::isNotBlank)
        ?.let { expandHomeVariable(it, home) }
    if (envValue != null) {
        val path = Path(envValue)
        path.assertExists()
        return PlatformFile(path)
    }

    // Fallback: the xdg-user-dirs configuration file written by xdg-user-dirs-update
    val configuredValue = readXdgUserDirsConfig(home)[envKey]
        ?.takeIf(String::isNotBlank)
        ?.let { expandHomeVariable(it, home) }
    if (configuredValue != null) {
        val path = Path(configuredValue)
        path.assertExists()
        return PlatformFile(path)
    }

    // Last resort: the default English folder name inside HOME (same as JVM)
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

private fun expandHomeVariable(path: String, home: String): String =
    path
        .replace("\${HOME}", home)
        .replace("\$HOME", home)

private fun readXdgUserDirsConfig(home: String): Map<String, String> {
    val configHome = getEnv("XDG_CONFIG_HOME")?.takeIf { it.startsWith("/") } ?: "$home/.config"
    val configFile = Path(configHome, "user-dirs.dirs")
    if (!SystemFileSystem.exists(configFile)) return emptyMap()

    val content = runCatching {
        SystemFileSystem.source(configFile).buffered().use { it.readString() }
    }.getOrNull() ?: return emptyMap()

    return parseXdgUserDirsConfig(content)
}

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
