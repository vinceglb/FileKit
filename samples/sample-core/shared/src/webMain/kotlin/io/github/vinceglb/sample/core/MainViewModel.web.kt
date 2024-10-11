package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun pickDirectoryIfSupported(
    platformSettings: FileKitDialogSettings?
): PlatformFile? = null

actual fun downloadDirectoryPath(): String? = null
