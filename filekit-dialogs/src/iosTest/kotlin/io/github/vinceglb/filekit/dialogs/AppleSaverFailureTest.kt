@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppleSaverFailureTest {
    @Test
    fun AppleSaver_missingPreparationResource_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            requireAppleDialogResource<NSURL>(
                resource = null,
                failureMessage = "Failed to prepare a temporary file for saving.",
            )
        }

        assertEquals("Failed to prepare a temporary file for saving.", failure.message)
    }

    @Test
    fun AppleSaver_failedPreparationOperation_throwsDialogOperationalFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            requireAppleDialogCondition(
                satisfied = false,
                failureMessage = "Failed to write the temporary file for saving.",
            )
        }

        assertEquals("Failed to write the temporary file for saving.", failure.message)
    }
}
