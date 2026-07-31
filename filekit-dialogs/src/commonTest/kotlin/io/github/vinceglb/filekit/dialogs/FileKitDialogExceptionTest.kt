@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FileKitDialogExceptionTest {
    @Test
    fun FileKitPickerException_isADialogException_withItsCausePreserved() {
        val cause = IllegalStateException("provider failed")
        val failure = FileKitPickerException("Failed to load the selected file.", cause)

        assertIs<FileKitDialogException>(failure)
        assertEquals(cause, failure.cause)
    }
}
