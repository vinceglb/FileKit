@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import java.io.File
import javax.swing.JFileChooser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SwingFilePickerResultTest {
    @Test
    fun SwingResult_cancel_returnsNull() {
        assertNull(resolveSwingDialogResult(JFileChooser.CANCEL_OPTION, emptyArray(), null))
    }

    @Test
    fun SwingResult_approvedSelection_returnsFiles() {
        val selectedFile = File("selected.txt")

        assertEquals(
            listOf(selectedFile),
            resolveSwingDialogResult(JFileChooser.APPROVE_OPTION, emptyArray(), selectedFile),
        )
    }

    @Test
    fun SwingResult_approvedWithoutSelection_throwsOperationFailure() {
        assertFailsWith<JvmDialogOperationException> {
            resolveSwingDialogResult(JFileChooser.APPROVE_OPTION, emptyArray(), null)
        }
    }

    @Test
    fun SwingResult_error_throwsOperationFailure() {
        assertFailsWith<JvmDialogOperationException> {
            resolveSwingDialogResult(JFileChooser.ERROR_OPTION, emptyArray(), null)
        }
    }
}
