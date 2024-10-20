package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import io.github.vinceglb.filekit.utils.div
import io.github.vinceglb.filekit.utils.toFile
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

public actual object FileKit {
    private var _appId: String? = null
    public val appId: String
        get() = _appId ?: throw FileKitNotInitializedException()

    public fun init(appId: String) {
        _appId = appId
    }
}

public actual val FileKit.filesDir: PlatformFile
    get() = when(PlatformUtil.current) {
        Platform.Linux -> getEnv("HOME").toPath() / ".local" / "share" / appId
        Platform.MacOS -> getEnv("HOME").toPath() / "Library" / "Application Support" / appId
        Platform.Windows -> getEnv("APPDATA").toPath() / appId
    }.also(Path::assertExists).let(::PlatformFile)

public actual val FileKit.cacheDir: PlatformFile
    get() = when(PlatformUtil.current) {
        Platform.Linux -> getEnv("HOME").toPath() / ".cache" / appId
        Platform.MacOS -> getEnv("HOME").toPath() / "Library" / "Caches" / appId
        Platform.Windows -> getEnv("LOCALAPPDATA").toPath() / appId / "Cache"
    }.also(Path::assertExists).let(::PlatformFile)

private fun getEnv(key: String): String {
    return System.getenv(key)
        ?: throw IllegalStateException("Environment variable $key not found.")
}

private fun Path.assertExists() {
    if (!SystemFileSystem.exists(this)) {
        this.toFile().mkdirs()
    }
}
