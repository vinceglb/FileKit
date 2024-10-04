package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.pickDirectory
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitPlatformSettings?): PlatformFile? {
    return FileKit.pickDirectory(platformSettings = platformSettings)
}
