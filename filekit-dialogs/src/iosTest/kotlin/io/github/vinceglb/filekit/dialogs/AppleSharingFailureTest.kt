@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import platform.Foundation.NSError
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class AppleSharingFailureTest {
    @Test
    fun AppleSharing_completionError_returnsDialogOperationalFailureWithCause() {
        val nativeError = NSError.errorWithDomain(
            domain = "io.github.vinceglb.filekit.tests",
            code = 42,
            userInfo = null,
        )

        val failure = assertIs<FileKitDialogException>(appleShareCompletionFailure(nativeError))

        assertEquals("The share operation failed: ${nativeError.localizedDescription}", failure.message)
        val cause = assertIs<AppleShareExceptionCause>(failure.cause)
        assertSame(nativeError, cause.error)
    }

    @Test
    fun AppleSharing_completionWithoutError_returnsNoFailure() {
        assertNull(appleShareCompletionFailure(null))
    }

    @Test
    fun AppleSharing_missingPresenter_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            requireAppleSharePresenter(null)
        }

        assertEquals("No active view controller is available to present the share sheet.", failure.message)
    }

    @Test
    fun AppleSharing_availablePresenter_isReturned() {
        val presenter = UIViewController()

        assertSame(presenter, requireAppleSharePresenter(presenter))
    }
}
