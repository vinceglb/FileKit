package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIActivityViewController

public actual open class FileKitShareSettings(
    public val metaTitle: String = "Share File",
    public val addOptionUIActivityViewController: (UIActivityViewController) -> Unit = {},
) {
    public actual companion object {
        public actual fun createDefault(): FileKitShareSettings = FileKitShareSettings()
    }
}
