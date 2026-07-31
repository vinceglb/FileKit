@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JvmDialogFailureTest {
    @Test
    fun runJvmDialogOperation_knownNativeFailure_wrapsAndPreservesCause() = runTest {
        val nativeFailure = UnsupportedOperationException("Dialog unavailable")

        val failure = assertFailsWith<FileKitDialogException> {
            runJvmDialogOperation(
                failure = { FileKitDialogException("Failed to open the dialog.", it) },
                block = { throw JvmDialogOperationException(nativeFailure) },
            )
        }

        assertSame(nativeFailure, failure.cause)
    }

    @Test
    fun runJvmDialogOperation_unexpectedRuntimeFailure_propagates() = runTest {
        val defect = IllegalStateException("Unexpected invariant violation")

        val failure = assertFailsWith<IllegalStateException> {
            runJvmDialogOperation(
                failure = { FileKitDialogException("Failed to open the dialog.", it) },
                block = { throw defect },
            )
        }

        assertSame(defect, failure)
    }

    @Test
    fun runJvmDialogOperation_invalidArgument_propagates() = runTest {
        val invalidArgument = IllegalArgumentException("Unsupported mode")

        val failure = assertFailsWith<IllegalArgumentException> {
            runJvmDialogOperation(
                failure = { FileKitDialogException("Failed to open the dialog.", it) },
                block = { throw invalidArgument },
            )
        }

        assertSame(invalidArgument, failure)
    }
}
