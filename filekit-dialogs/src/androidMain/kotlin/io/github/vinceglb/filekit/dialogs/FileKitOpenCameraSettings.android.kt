package io.github.vinceglb.filekit.dialogs

/**
 * Android implementation of [FileKitOpenCameraSettings].
 *
 * @property authority The content authority string used for creating a [android.net.Uri].
 * Defaults to "{applicationId}.FileKitFileProvider" when null.
 */
public actual class FileKitOpenCameraSettings(
    public val authority: String? = null,
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenCameraSettings].
         */
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
