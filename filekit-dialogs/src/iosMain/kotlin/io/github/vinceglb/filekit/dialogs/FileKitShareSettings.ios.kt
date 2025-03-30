package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIActivityViewController

public actual open class FileKitShareSettings(
    public val metaTitle: String,
    public val addOptionUIActivityViewController: (UIActivityViewController) -> Unit
) {
    public constructor() : this(
        metaTitle = "Share Image",
        addOptionUIActivityViewController = {}
    )
}

public class FileKitIOSDefaultShareSettings() : FileKitShareSettings()