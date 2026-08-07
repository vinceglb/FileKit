@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import platform.windows.COINIT_MULTITHREADED
import platform.windows.CoInitializeEx
import platform.windows.CoUninitialize
import platform.windows.S_OK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class)
class WindowsNativePickerFailureTest {
    @Test
    fun SinglePicker_incompatibleComApartment_throwsPickerOperationalFailureWithCause() = runTest {
        assertIncompatibleComApartmentFailure(FileKitMode.Single)
    }

    @Test
    fun MultiplePicker_incompatibleComApartment_throwsPickerOperationalFailureWithCause() = runTest {
        assertIncompatibleComApartmentFailure(FileKitMode.Multiple())
    }

    private suspend fun <PickerResult, ConsumedResult> assertIncompatibleComApartmentFailure(
        mode: FileKitMode<PickerResult, ConsumedResult>,
    ) {
        val initializationResult = CoInitializeEx(null, COINIT_MULTITHREADED)
        assertEquals(S_OK, initializationResult)

        try {
            val failure = assertFailsWith<FileKitPickerException> {
                FileKit.openFilePicker(
                    type = FileKitType.File(),
                    mode = mode,
                )
            }

            assertEquals("The Windows file picker could not complete the operation.", failure.message)
            val cause = assertNotNull(failure.cause)
            assertIs<WindowsDialogOperationalException>(cause)
            assertEquals("CoInitializeEx failed with HRESULT 0x80010106", cause.message)
        } finally {
            CoUninitialize()
        }
    }

    @Test
    fun OpenPicker_cancelledDialog_returnsNullWithoutResolvingSelection() {
        var selectionResolved = false

        val result = handleWindowsNativeDialogResult(
            result = ERROR_CANCELLED_HRESULT,
            failurePolicy = WindowsDialogFailurePolicy.Picker,
            operation = "IFileOpenDialog::Show",
        ) {
            selectionResolved = true
            "selected.txt"
        }

        assertNull(result)
        assertFalse(selectionResolved)
    }

    @Test
    fun PickerOperation_unexpectedFailure_propagatesUnchanged() {
        val sentinel = UnexpectedPickerFailure()

        val thrown = assertFailsWith<UnexpectedPickerFailure> {
            runWindowsNativePickerOperation {
                throw sentinel
            }
        }

        assertSame(sentinel, thrown)
    }

    @Test
    fun MultiplePicker_invalidMaxItems_failsFast() = runTest {
        val failure = assertFailsWith<IllegalArgumentException> {
            FileKit.openFilePicker(
                type = FileKitType.File(),
                mode = FileKitMode.Multiple(maxItems = 0),
            )
        }

        assertEquals(
            "maxItems must be contained between 1 <= maxItems <= 50 but current value is 0",
            failure.message,
        )
    }

    private companion object {
        val ERROR_CANCELLED_HRESULT = 0x800704C7u.toInt()
    }

    private class UnexpectedPickerFailure : RuntimeException()
}
