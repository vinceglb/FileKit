package io.github.vinceglb.sample.core

import io.github.vinceglb.filekit.dialog.FileKit
import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.pickDirectory
import io.github.vinceglb.filekit.PlatformFile

actual suspend fun pickDirectoryIfSupported(platformSettings: FileKitDialogSettings?): PlatformFile? {
    return FileKit.pickDirectory(platformSettings = platformSettings)
}
