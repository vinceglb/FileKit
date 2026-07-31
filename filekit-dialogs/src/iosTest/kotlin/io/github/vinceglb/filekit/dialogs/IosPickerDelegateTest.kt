@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.dialogs.util.DocumentPickerDelegate
import io.github.vinceglb.filekit.dialogs.util.PhPickerDelegate
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerViewController
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class IosPickerDelegateTest {
    @Test
    fun DocumentPickerDelegate_cancellation_reportsResultAfterDismissal() {
        lateinit var finishDismissal: () -> Unit
        var resultReported = false
        val delegate = DocumentPickerDelegate(
            onFilesPicked = {},
            onPickerCancelled = { resultReported = true },
            dismiss = { _, finish -> finishDismissal = finish },
        )
        val controller = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem))

        delegate.documentPickerWasCancelled(controller)

        assertFalse(resultReported)
        finishDismissal()
        assertTrue(resultReported)
    }

    @Test
    fun PhPickerDelegate_selection_reportsResultAfterDismissal() {
        lateinit var finishDismissal: () -> Unit
        var resultReported = false
        val delegate = PhPickerDelegate(
            onFilesPicked = { resultReported = true },
            dismiss = { _, finish -> finishDismissal = finish },
        )
        val controller = PHPickerViewController(
            configuration = PHPickerConfiguration(sharedPhotoLibrary()),
        )

        delegate.picker(controller, emptyList<Any>())

        assertFalse(resultReported)
        finishDismissal()
        assertTrue(resultReported)
    }
}
