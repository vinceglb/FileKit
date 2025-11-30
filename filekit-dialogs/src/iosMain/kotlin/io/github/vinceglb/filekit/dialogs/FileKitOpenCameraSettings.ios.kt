package io.github.vinceglb.filekit.dialogs

/**
 * iOS implementation of [FileKitOpenCameraSettings].
 * Currently, there are no specific settings for opening the camera on iOS.
 */
public actual class FileKitOpenCameraSettings {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenCameraSettings].
         */
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
