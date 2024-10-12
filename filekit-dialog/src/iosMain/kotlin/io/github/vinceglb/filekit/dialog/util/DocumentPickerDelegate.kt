package io.github.vinceglb.filekit.dialog.util

import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

internal class DocumentPickerDelegate(
    private val onFilesPicked: (List<NSURL>) -> Unit,
    private val onPickerCancelled: () -> Unit
) : NSObject(),
    UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentAtURL: NSURL
    ) {
        onFilesPicked(listOf(didPickDocumentAtURL))
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val res = didPickDocumentsAtURLs.mapNotNull { it as? NSURL }
        onFilesPicked(res)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPickerCancelled()
    }
}
