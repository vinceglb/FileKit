package io.github.vinceglb.picker.core.util

import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject

internal class PickerDelegate(
	private val onFilesPicked: (List<NSURL>) -> Unit,
	private val onPickerCancelled: () -> Unit
) : NSObject(),
	UIDocumentPickerDelegateProtocol {
	override fun documentPicker(
		controller: UIDocumentPickerViewController,
		didPickDocumentsAtURLs: List<*>
	) {
		println("documentPicker called ${didPickDocumentsAtURLs.size} files picked")
		val res = didPickDocumentsAtURLs.mapNotNull { it as? NSURL }
		onFilesPicked(res)
	}

	override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
		println("Picker was cancelled")
		onPickerCancelled()
	}
}
