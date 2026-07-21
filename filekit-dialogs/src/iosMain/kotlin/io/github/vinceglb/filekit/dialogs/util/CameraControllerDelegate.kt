package io.github.vinceglb.filekit.dialogs.util

import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

internal class CameraControllerDelegate(
    private val onImagePicked: (UIImage?) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        // Deliver the result only once the picker is fully dismissed, so client reactions
        // (navigation, closing dialogs, ...) can't race the UIKit dismissal transition
        picker.dismissViewControllerAnimated(true) {
            onImagePicked.invoke(image)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onImagePicked.invoke(null)
        }
    }
}
