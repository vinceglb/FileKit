package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

public actual class FileKitOpenCameraSettings(
    public val authority: String = "${FileKit.context.packageName}.FileKitFileProvider",
) {
    public actual companion object {
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
