package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.utils.div
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.io.files.Path

internal fun resolveLinuxUserDirectoryPath(
    type: FileKitUserDirectory,
    home: String?,
    envProvider: (String) -> String?,
    linuxUserDirsConfigProvider: () -> String?,
): Path? {
    val safeHome = home
        ?.takeIf(String::isNotBlank)
        ?: return null
    val envValue = envProvider(type.xdgEnvKey)
        ?.takeIf(String::isNotBlank)
        ?.let { expandHomeVariable(it, safeHome) }
    if (envValue != null) {
        return envValue.toPath()
    }

    val configuredValue = linuxUserDirsConfigProvider()
        ?.takeIf(String::isNotBlank)
        ?.let(::parseXdgUserDirsConfig)
        ?.get(type.xdgEnvKey)
        ?.takeIf(String::isNotBlank)
        ?.let { expandHomeVariable(it, safeHome) }
    if (configuredValue != null) {
        return configuredValue.toPath()
    }

    return safeHome.toPath() / type.linuxFallbackDirName
}

internal fun parseXdgUserDirsConfig(config: String): Map<String, String> =
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

private fun expandHomeVariable(path: String, home: String): String =
    path
        .replace("\${HOME}", home)
        .replace("\$HOME", home)

private val FileKitUserDirectory.xdgEnvKey: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "XDG_DOWNLOAD_DIR"
        FileKitUserDirectory.Pictures -> "XDG_PICTURES_DIR"
        FileKitUserDirectory.Videos -> "XDG_VIDEOS_DIR"
        FileKitUserDirectory.Music -> "XDG_MUSIC_DIR"
        FileKitUserDirectory.Documents -> "XDG_DOCUMENTS_DIR"
    }

private val FileKitUserDirectory.linuxFallbackDirName: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Videos"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }
