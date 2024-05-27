package io.github.vinceglb.picker.core.util

import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

internal class PhPickerDelegate(
    private val onFilesPicked: (List<PHPickerResult>) -> Unit
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        // Dismiss the picker
        picker.dismissViewControllerAnimated(true, null)

        // Map the results to PHPickerResult
        val res = didFinishPicking.mapNotNull { it as? PHPickerResult }
        onFilesPicked(res)
    }
}
