package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PlatformFile

actual suspend fun pickDirectoryIfSupported(
    platformSettings: FileKitPlatformSettings?
): PlatformFile? = null

actual fun downloadDirectoryPath(): String? = null
