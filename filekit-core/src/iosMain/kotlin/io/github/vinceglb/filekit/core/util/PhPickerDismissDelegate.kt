package io.github.vinceglb.filekit.core.util

import platform.PhotosUI.PHPickerResult
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIPresentationController
import platform.darwin.NSObject

internal class PhPickerDismissDelegate(
    private val onFilesPicked: (List<PHPickerResult>) -> Unit
) : NSObject(),
    UIAdaptivePresentationControllerDelegateProtocol {
    override fun presentationControllerDidDismiss(presentationController: UIPresentationController) {
        onFilesPicked(listOf())
    }
}
