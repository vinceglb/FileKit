package io.github.vinceglb.filekit.dialogs.util

import platform.Foundation.NSURL
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.darwin.NSObject

internal class DocumentPickerDelegate(
    private val onFilesPicked: (List<NSURL>) -> Unit,
    private val onPickerCancelled: () -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol,
    UIAdaptivePresentationControllerDelegateProtocol {
    private var hasFinished = false

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentAtURL: NSURL,
    ) {
        if (finishOnce()) {
            onFilesPicked(listOf(didPickDocumentAtURL))
        }
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        if (finishOnce()) {
            onFilesPicked(didPickDocumentsAtURLs.mapNotNull { it as? NSURL })
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        if (finishOnce()) {
            onPickerCancelled()
        }
    }

    /**
     * Swiping the sheet away does not always reach [documentPickerWasCancelled] — notably when the
     * dismissal starts before the presentation animation has finished. Without this the caller's
     * continuation waits for a delegate call that never comes. See #138.
     */
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        if (finishOnce()) {
            onPickerCancelled()
        }
    }

    /**
     * A dismissal often reports through both protocols, and the caller resumes a continuation that
     * only accepts one answer, so every entry point above goes through this.
     */
    private fun finishOnce(): Boolean {
        if (hasFinished) return false
        hasFinished = true
        return true
    }
}
