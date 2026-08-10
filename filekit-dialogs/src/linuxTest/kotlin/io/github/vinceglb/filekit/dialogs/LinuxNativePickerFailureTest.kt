@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.dialogs.platform.linux.LinuxXdgPortalException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class LinuxNativePickerFailureTest {
    @Test
    fun PickerOperation_portalFailure_wrapsIntoPickerOperationalFailureWithCause() {
        val failure = assertFailsWith<FileKitPickerException> {
            runLinuxNativePickerOperation {
                throw LinuxXdgPortalException("The portal could not complete the operation.")
            }
        }

        assertEquals("The Linux file picker could not complete the operation.", failure.message)
        val cause = assertNotNull(failure.cause)
        assertIs<LinuxXdgPortalException>(cause)
        assertEquals("The portal could not complete the operation.", cause.message)
    }

    @Test
    fun PickerOperation_unexpectedFailure_propagatesUnchanged() {
        val sentinel = UnexpectedPickerFailure()

        val thrown = assertFailsWith<UnexpectedPickerFailure> {
            runLinuxNativePickerOperation {
                throw sentinel
            }
        }

        assertSame(sentinel, thrown)
    }

    @Test
    fun PickerOperation_success_returnsOperationResult() {
        val result = runLinuxNativePickerOperation {
            listOf("/tmp/selected.txt")
        }

        assertEquals(listOf("/tmp/selected.txt"), result)
    }

    private class UnexpectedPickerFailure : RuntimeException()
}
