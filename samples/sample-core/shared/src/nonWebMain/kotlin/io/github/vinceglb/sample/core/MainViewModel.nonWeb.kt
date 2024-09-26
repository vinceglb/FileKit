package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PlatformFile
import io.github.vinceglb.filekit.core.pickDirectory

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitPlatformSettings?): PlatformFile? {
    return FileKit.pickDirectory(platformSettings = platformSettings)
}
