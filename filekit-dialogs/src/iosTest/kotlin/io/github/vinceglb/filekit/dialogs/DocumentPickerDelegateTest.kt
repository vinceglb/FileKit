@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.dialogs.util.DocumentPickerDelegate
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIPresentationController
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The caller resumes a continuation from these callbacks, and a continuation only accepts one
 * answer, so what matters is that exactly one of them ever gets through.
 */
class DocumentPickerDelegateTest {
    @Test
    fun DocumentPickerDelegate_swipeDismissWithoutCancelCallback_reportsCancelled() {
        val recorder = Recorder()

        recorder.delegate.presentationControllerDidDismiss(presentationController())

        assertEquals(expected = 0, actual = recorder.pickedCount)
        assertEquals(expected = 1, actual = recorder.cancelledCount)
    }

    @Test
    fun DocumentPickerDelegate_cancelThenSwipeDismiss_reportsCancelledOnce() {
        val recorder = Recorder()

        recorder.delegate.documentPickerWasCancelled(picker())
        recorder.delegate.presentationControllerDidDismiss(presentationController())

        assertEquals(expected = 1, actual = recorder.cancelledCount)
    }

    @Test
    fun DocumentPickerDelegate_pickThenSwipeDismiss_reportsOnlyThePick() {
        val recorder = Recorder()

        recorder.delegate.documentPicker(picker(), didPickDocumentAtURL = NSURL(string = "file:///tmp/a.txt"))
        recorder.delegate.presentationControllerDidDismiss(presentationController())

        assertEquals(expected = 1, actual = recorder.pickedCount)
        assertEquals(expected = 0, actual = recorder.cancelledCount)
    }

    private class Recorder {
        var pickedCount = 0
        var cancelledCount = 0

        val delegate = DocumentPickerDelegate(
            onFilesPicked = { pickedCount++ },
            onPickerCancelled = { cancelledCount++ },
        )
    }

    private fun picker(): UIDocumentPickerViewController =
        UIDocumentPickerViewController(forOpeningContentTypes = emptyList<Any>())

    private fun presentationController(): UIPresentationController =
        UIPresentationController(
            presentedViewController = UIViewController(nibName = null, bundle = null),
            presentingViewController = null,
        )
}
