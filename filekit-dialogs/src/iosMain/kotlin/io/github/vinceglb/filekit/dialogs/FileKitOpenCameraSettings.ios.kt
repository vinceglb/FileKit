package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIViewController

/**
 * iOS implementation of [FileKitOpenCameraSettings].
 *
 * @property presenter The view controller used to present the camera picker. When null, FileKit
 * presents the camera from a dedicated window placed above the app's windows, which keeps the
 * picker compatible with hosts whose dialogs live in their own window, such as Compose
 * Multiplatform 1.11+.
 */
public actual class FileKitOpenCameraSettings(
    public val presenter: UIViewController? = null,
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenCameraSettings].
         */
        public actual fun createDefault(): FileKitOpenCameraSettings = FileKitOpenCameraSettings()
    }
}
