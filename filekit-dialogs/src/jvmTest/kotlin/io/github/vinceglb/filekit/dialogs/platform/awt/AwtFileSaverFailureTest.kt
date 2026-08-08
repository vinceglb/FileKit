@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import kotlinx.coroutines.test.runTest
import java.awt.HeadlessException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AwtFileSaverFailureTest {
    @Test
    fun AwtFileSaver_headlessFailure_throwsDialogOperationalFailureWithCause() = runTest {
        val headlessFailure = HeadlessException("No graphics environment")

        val failure = assertFailsWith<FileKitDialogException> {
            runAwtFileSaver {
                throw headlessFailure
            }
        }

        assertSame(headlessFailure, failure.cause)
    }
}
