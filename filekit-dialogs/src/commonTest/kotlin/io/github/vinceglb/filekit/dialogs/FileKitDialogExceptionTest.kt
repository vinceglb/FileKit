@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.exceptions.FileKitException
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class FileKitDialogExceptionTest {
    @Test
    fun FileKitPickerException_isAFileKitDialogException_andPreservesCause() {
        val cause = IllegalStateException("Native picker failed")

        val failure = FileKitPickerException("Could not open the picker", cause)

        assertIs<FileKitDialogException>(failure)
        assertIs<FileKitException>(failure)
        assertSame(cause, failure.cause)
    }
}
