@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import kotlinx.coroutines.test.runTest
import java.awt.HeadlessException
import java.io.File
import javax.swing.JFileChooser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class SwingDirectoryPickerResultTest {
    @Test
    fun SwingDirectoryPicker_approvedSelection_returnsSelectedDirectory() {
        val directory = File("selected-directory")

        val result = resolveSwingPickerResult(
            returnValue = JFileChooser.APPROVE_OPTION,
            selectedFiles = emptyArray(),
            selectedFile = directory,
        )

        assertEquals(listOf(directory), result)
    }

    @Test
    fun SwingDirectoryPicker_cancelledSelection_returnsNull() {
        val result = resolveSwingPickerResult(
            returnValue = JFileChooser.CANCEL_OPTION,
            selectedFiles = emptyArray(),
            selectedFile = null,
        )

        assertNull(result)
    }

    @Test
    fun SwingDirectoryPicker_headlessFailure_throwsDialogOperationalFailureWithCause() = runTest {
        val headlessFailure = HeadlessException("No graphics environment")

        val failure = assertFailsWith<FileKitDialogException> {
            runSwingDirectoryPicker {
                throw headlessFailure
            }
        }

        assertSame(headlessFailure, failure.cause)
    }
}
