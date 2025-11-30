package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.context

/**
 * Android implementation of [FileKitOpenCameraSettings].
 *
 * @property authority The content authority string used for creating a [android.net.Uri].
 * Defaults to "{applicationId}.FileKitFileProvider".
 */
public actual class FileKitOpenCameraSettings(
    public val authority: String = "${FileKit.context.packageName}.FileKitFileProvider",
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenCameraSettings].
         */
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
