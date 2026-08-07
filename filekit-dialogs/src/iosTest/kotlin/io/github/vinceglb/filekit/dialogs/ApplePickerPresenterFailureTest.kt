@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplePickerPresenterFailureTest {
    @Test
    fun DocumentPicker_missingPresenter_reportsPickerFailure_withoutResult() {
        assertMissingPresenterFailure<FileKitPickerException>(ApplePickerPresentationOperation.Document)
    }

    @Test
    fun PhotoVideoPicker_missingPresenter_reportsPickerFailure_withoutResult() {
        assertMissingPresenterFailure<FileKitPickerException>(ApplePickerPresentationOperation.PhotoOrVideo)
    }

    @Test
    fun DirectoryPicker_missingPresenter_reportsDialogFailure_withoutCancellationResult() {
        assertMissingPresenterFailure<FileKitDialogException>(ApplePickerPresentationOperation.Directory)
    }

    private inline fun <reified Failure : FileKitDialogException> assertMissingPresenterFailure(
        operation: ApplePickerPresentationOperation,
    ) {
        var activePresenterResolutionCount = 0
        var resultCount = 0
        val failures = mutableListOf<FileKitDialogException>()

        try {
            presentApplePickerController(
                dialogSettings = FileKitDialogSettings(presenter = null),
                controller = UIViewController(),
                operation = operation,
                activeViewController = {
                    activePresenterResolutionCount++
                    null
                },
            )
            resultCount++
        } catch (failure: FileKitDialogException) {
            failures += failure
        }

        assertEquals(1, activePresenterResolutionCount)
        assertEquals(0, resultCount)
        assertEquals(1, failures.size)
        assertEquals(Failure::class, failures.single()::class)
    }
}
