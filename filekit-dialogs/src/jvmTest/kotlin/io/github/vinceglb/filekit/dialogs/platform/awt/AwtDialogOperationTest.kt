@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import java.awt.HeadlessException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AwtDialogOperationTest {
    @Test
    fun AwtDialogOperation_headlessFailure_wrapsCause() {
        val headlessFailure = HeadlessException("No display")

        val failure = assertFailsWith<JvmDialogOperationException> {
            runAwtDialogOperation<Unit>("Failed to present dialog") { throw headlessFailure }
        }

        assertEquals("Failed to present dialog", failure.message)
        assertSame(headlessFailure, failure.cause)
    }

    @Test
    fun AwtDialogOperation_unexpectedFailure_propagates() {
        val unexpectedFailure = IllegalArgumentException("Unexpected")

        val failure = assertFailsWith<IllegalArgumentException> {
            runAwtDialogOperation<Unit>("Failed to present dialog") { throw unexpectedFailure }
        }

        assertSame(unexpectedFailure, failure)
    }

    @Test
    fun AwtDialogDispatch_cancelledBeforeDispatch_skipsPresentation() {
        var presented = false

        dispatchAwtDialogOperation(
            isActive = { false },
            operation = {
                presented = true
            },
            onResult = {},
            onFailure = {},
        )

        assertEquals(false, presented)
    }

    @Test
    fun AwtDialogDispatch_unexpectedFailure_forwardsOriginalFailure() {
        val unexpectedFailure = IllegalArgumentException("Unexpected")
        var reportedFailure: Throwable? = null

        dispatchAwtDialogOperation(
            isActive = { true },
            operation = { throw unexpectedFailure },
            onResult = {},
            onFailure = { reportedFailure = it },
        )

        assertSame(unexpectedFailure, reportedFailure)
    }
}
