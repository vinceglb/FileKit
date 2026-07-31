package io.github.vinceglb.filekit.dialogs.util

import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

internal class PhPickerDelegate(
    private val onFilesPicked: (List<PHPickerResult>) -> Unit,
    private val dismiss: (PHPickerViewController, finishDismissal: () -> Unit) -> Unit =
        { picker, finishDismissal ->
            picker.dismissViewControllerAnimated(true, finishDismissal)
        },
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {
    private var hasFinished = false

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        if (hasFinished) return
        hasFinished = true

        // Map the results to PHPickerResult
        val res = didFinishPicking.mapNotNull { it as? PHPickerResult }

        // Keep the delegate and presenter reservation until the dismissal transition completes.
        dismiss(picker) {
            onFilesPicked(res)
        }
    }
}
