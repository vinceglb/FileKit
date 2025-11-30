package io.github.vinceglb.filekit.dialogs.util

import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.darwin.NSObject

internal class PhPickerDelegate(
    private val onFilesPicked: (List<PHPickerResult>) -> Unit
) : NSObject(),
    PHPickerViewControllerDelegateProtocol,
    UIAdaptivePresentationControllerDelegateProtocol {
    private var hasFinished = false

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        if (hasFinished) return
        hasFinished = true

        // Dismiss the picker
        picker.dismissViewControllerAnimated(true, null)

        // Map the results to PHPickerResult
        val res = didFinishPicking.mapNotNull { it as? PHPickerResult }
        onFilesPicked(res)
    }

    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        if (hasFinished) return
        hasFinished = true
        onFilesPicked(listOf())
    }
}
