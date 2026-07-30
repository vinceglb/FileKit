package io.github.vinceglb.filekit

import com.sun.jna.platform.win32.KnownFolders
import com.sun.jna.platform.win32.Shell32Util
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.div
import io.github.vinceglb.filekit.utils.toPath
import kotlinx.io.files.Path
import java.io.File

internal fun resolveJvmUserDirectoryPath(
    type: FileKitUserDirectory,
    platform: Platform,
    envProvider: (String) -> String?,
    linuxUserDirsConfigProvider: () -> String?,
    windowsKnownFolderResolver: (FileKitUserDirectory) -> String?,
): Path? = when (platform) {
    Platform.Linux -> resolveLinuxUserDirectoryPath(
        type = type,
        home = envProvider("HOME"),
        envProvider = envProvider,
        linuxUserDirsConfigProvider = linuxUserDirsConfigProvider,
    )

    Platform.MacOS -> resolveMacUserDirectoryPath(
        type = type,
        home = envProvider("HOME"),
    )

    Platform.Windows -> resolveWindowsUserDirectoryPath(
        type = type,
        envProvider = envProvider,
        windowsKnownFolderResolver = windowsKnownFolderResolver,
    )
}

internal fun defaultLinuxUserDirsConfig(envProvider: (String) -> String?): String? {
    val home = envProvider("HOME")
        ?.takeIf(String::isNotBlank)
        ?: return null
    val configHome = envProvider("XDG_CONFIG_HOME")
        ?.takeIf(String::isNotBlank)
        ?: "$home/.config"
    val configFile = File(configHome, "user-dirs.dirs")
    return configFile.takeIf(File::exists)?.let { file ->
        runCatching(file::readText).getOrNull()
    }
}

internal fun resolveKnownFolderPath(type: FileKitUserDirectory): String? =
    runCatching {
        Shell32Util.getKnownFolderPath(
            when (type) {
                FileKitUserDirectory.Downloads -> KnownFolders.FOLDERID_Downloads
                FileKitUserDirectory.Pictures -> KnownFolders.FOLDERID_Pictures
                FileKitUserDirectory.Videos -> KnownFolders.FOLDERID_Videos
                FileKitUserDirectory.Music -> KnownFolders.FOLDERID_Music
                FileKitUserDirectory.Documents -> KnownFolders.FOLDERID_Documents
            },
        )
    }.getOrNull()

private fun resolveMacUserDirectoryPath(type: FileKitUserDirectory, home: String?): Path? {
    val safeHome = home?.takeIf(String::isNotBlank) ?: return null
    return (safeHome.toPath() / type.macFallbackDirName)
}

private fun resolveWindowsUserDirectoryPath(
    type: FileKitUserDirectory,
    envProvider: (String) -> String?,
    windowsKnownFolderResolver: (FileKitUserDirectory) -> String?,
): Path? {
    val knownFolderPath = windowsKnownFolderResolver(type)
        ?.takeIf(String::isNotBlank)
    if (knownFolderPath != null) {
        return knownFolderPath.toPath()
    }

    val userProfile = envProvider("USERPROFILE")
        ?.takeIf(String::isNotBlank)
        ?: return null
    return (userProfile.toPath() / type.windowsFallbackDirName)
}

private val FileKitUserDirectory.macFallbackDirName: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Movies"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }

private val FileKitUserDirectory.windowsFallbackDirName: String
    get() = when (this) {
        FileKitUserDirectory.Downloads -> "Downloads"
        FileKitUserDirectory.Pictures -> "Pictures"
        FileKitUserDirectory.Videos -> "Videos"
        FileKitUserDirectory.Music -> "Music"
        FileKitUserDirectory.Documents -> "Documents"
    }
