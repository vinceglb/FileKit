@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.platform.win32.W32Errors.HRESULT_FROM_WIN32
import com.sun.jna.platform.win32.WinError.ERROR_CANCELLED
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

class WindowsFilePickerFailureTest {
    @Test
    fun WindowsFilePicker_comInitializationFailure_throwsPickerOperationalFailureWithCause() = runTest {
        val executor = failingWindowsDialogExecutor()

        try {
            val failure = assertFailsWith<FileKitPickerException> {
                WindowsFilePicker(executor).openFilePicker(
                    fileExtensions = null,
                    directory = null,
                    dialogSettings = FileKitDialogSettings(),
                )
            }

            assertIs<WindowsDialogOperationalException>(failure.cause)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsFilesPicker_comInitializationFailure_throwsPickerOperationalFailureWithCause() = runTest {
        val executor = failingWindowsDialogExecutor()

        try {
            val failure = assertFailsWith<FileKitPickerException> {
                WindowsFilePicker(executor).openFilesPicker(
                    fileExtensions = null,
                    directory = null,
                    dialogSettings = FileKitDialogSettings(),
                )
            }

            assertIs<WindowsDialogOperationalException>(failure.cause)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsFilePicker_cancelledDialog_returnsNullWithoutResolvingSelection() {
        var selectionResolved = false

        val result = handleWindowsDialogResult(HRESULT_FROM_WIN32(ERROR_CANCELLED)) {
            selectionResolved = true
            "selected.txt"
        }

        assertNull(result)
        assertFalse(selectionResolved)
    }

    private fun failingWindowsDialogExecutor(): WindowsDialogExecutor = WindowsDialogExecutor(
        comRuntime = object : WindowsComRuntime {
            override fun initializeSta(): Int = E_OUTOFMEMORY

            override fun uninitialize() = Unit
        },
    )

    private companion object {
        val E_OUTOFMEMORY = 0x8007000Eu.toInt()
    }
}
