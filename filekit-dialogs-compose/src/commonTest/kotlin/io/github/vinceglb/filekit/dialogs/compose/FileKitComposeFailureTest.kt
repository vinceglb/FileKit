@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileKitComposeFailureTest {
    @Test
    fun runFilePickerLauncher_reportsPickerException_withoutInvokingResult() = runTest {
        val failure = FileKitPickerException("Failed to load the selected file.")
        var reportedFailure: FileKitPickerException? = null
        var resultInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.Single,
            openPicker = { throw failure },
            onError = { reportedFailure = it },
            onResult = { resultInvoked = true },
        )

        assertEquals(expected = failure, actual = reportedFailure)
        assertFalse(resultInvoked)
    }

    @Test
    fun runFilePickerLauncher_invokesResult_withoutInvokingError() = runTest {
        var errorInvoked = false
        var resultInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.Single,
            openPicker = { null },
            onError = { errorInvoked = true },
            onResult = { resultInvoked = true },
        )

        assertFalse(errorInvoked)
        assertTrue(resultInvoked)
    }
}
