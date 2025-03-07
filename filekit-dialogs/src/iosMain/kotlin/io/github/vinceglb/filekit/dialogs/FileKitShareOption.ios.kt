package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIActivityViewController

public actual open class FileKitShareOption(
    public val metaTitle: String,
    public val addOptionUIActivityViewController: (UIActivityViewController) -> Unit
) {
    public constructor() : this(
        metaTitle = "Share Image",
        addOptionUIActivityViewController = {}
    )
}

public class FileKitIOSDefaultShareOption() : FileKitShareOption()