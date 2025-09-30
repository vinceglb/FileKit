package io.github.vinceglb.filekit.dialogs.util

import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

internal class PhPickerDelegate(
    private val onFilesPicked: (List<PHPickerResult>) -> Unit
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {
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
}
