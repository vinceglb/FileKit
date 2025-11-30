package io.github.vinceglb.filekit.dialogs

/**
 * Settings for opening the camera.
 */
public expect class FileKitOpenCameraSettings {
    public companion object {
        /**
         * Creates a default instance of [FileKitOpenCameraSettings].
         */
        public fun createDefault(): FileKitOpenCameraSettings
    }
}
