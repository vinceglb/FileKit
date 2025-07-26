package io.github.vinceglb.filekit

import platform.UIKit.UIDocumentInteractionController

public actual fun PlatformFile.open() {
    val documentController = UIDocumentInteractionController()
    documentController.URL = this.nsUrl
    documentController.delegate()
    documentController.presentPreviewAnimated(
        animated = true
    )
}

