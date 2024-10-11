package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.pickDirectory

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitDialogSettings?): PlatformFile? {
    return FileKit.pickDirectory(platformSettings = platformSettings)
}
