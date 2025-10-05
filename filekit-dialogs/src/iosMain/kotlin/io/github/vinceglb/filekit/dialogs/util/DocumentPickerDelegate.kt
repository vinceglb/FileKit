package io.github.vinceglb.filekit.dialogs.util

import platform.Foundation.NSURL
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.darwin.NSObject

internal class DocumentPickerDelegate(
    private val onFilesPicked: (List<NSURL>) -> Unit,
    private val onPickerCancelled: () -> Unit
) : NSObject(),
    UIDocumentPickerDelegateProtocol,
    UIAdaptivePresentationControllerDelegateProtocol {
    
    private var hasFinished = false
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentAtURL: NSURL
    ) {
        if (hasFinished) return
        hasFinished = true
        onFilesPicked(listOf(didPickDocumentAtURL))
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        if (hasFinished) return
        hasFinished = true
        val res = didPickDocumentsAtURLs.mapNotNull { it as? NSURL }
        onFilesPicked(res)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        if (hasFinished) return
        hasFinished = true
        onPickerCancelled()
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        if (hasFinished) return
        hasFinished = true
        onPickerCancelled()
    }
}
