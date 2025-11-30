package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIActivityViewController

/**
 * iOS implementation of [FileKitShareSettings].
 *
 * @property metaTitle The title of the share sheet. Defaults to "Share File".
 * @property addOptionUIActivityViewController Callback to customize the [UIActivityViewController].
 */
public actual open class FileKitShareSettings(
    public val metaTitle: String = "Share File",
    public val addOptionUIActivityViewController: (UIActivityViewController) -> Unit = {},
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitShareSettings].
         */
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}
