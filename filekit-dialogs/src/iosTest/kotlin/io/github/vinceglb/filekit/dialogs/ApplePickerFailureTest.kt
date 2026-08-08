@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import platform.Foundation.NSError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class ApplePickerFailureTest {
    @Test
    fun ApplePicker_nativeFailure_preservesNSErrorAsPickerFailureCause() {
        val nativeError = NSError.errorWithDomain(
            domain = "io.github.vinceglb.filekit.tests",
            code = 42,
            userInfo = null,
        )

        val failure = applePickerFailure("Failed to load the selected file.", nativeError)

        val cause = assertIs<ApplePickerExceptionCause>(failure.cause)
        assertSame(nativeError, cause.error)
        assertEquals(nativeError.localizedDescription, cause.message)
    }

    @Test
    fun ApplePicker_failureWithoutNSError_hasNoSyntheticCause() {
        val failure = applePickerFailure("Failed to resolve the selected file.", null)

        assertNull(failure.cause)
    }
}
