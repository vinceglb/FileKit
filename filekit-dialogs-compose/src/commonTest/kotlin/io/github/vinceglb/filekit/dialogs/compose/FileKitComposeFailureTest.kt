@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FileKitComposeFailureTest {
    @Test
    fun legacyNullResultOnDialogFailure_reportsNullResult() {
        var result: String? = "unchanged"

        legacyNullResultOnDialogFailure<String> { result = it }
            .invoke(FileKitDialogException("Dialog failed"))

        assertEquals(null, result)
    }

    @Test
    fun legacyDirectoryFailure_reportsNullResult() {
        assertLegacyResultFailureReportsNull<String>()
    }

    @Test
    fun legacyFileSaverFailure_reportsNullResult() {
        assertLegacyResultFailureReportsNull<String>()
    }

    @Test
    fun legacyCameraFailure_reportsNullResult() {
        assertLegacyResultFailureReportsNull<String>()
    }

    @Test
    fun legacyShareFailure_isIgnored() {
        legacyIgnoreDialogFailure().invoke(FileKitDialogException("Share failed"))
    }

    @Test
    fun runDialogLauncher_successInvokesOnlyResult() = runTest {
        var errorInvoked = false
        var receivedResult: String? = null

        runDialogLauncher(
            openDialog = { "picked" },
            onError = { errorInvoked = true },
            onResult = { receivedResult = it },
        )

        assertFalse(errorInvoked)
        assertEquals("picked", receivedResult)
    }

    @Test
    fun runDialogLauncher_reportsDialogException_withoutInvokingResult() = runTest {
        val failure = FileKitDialogException("Failed to open the dialog.")
        var reportedFailure: FileKitDialogException? = null
        var resultInvoked = false

        runDialogLauncher(
            openDialog = { throw failure },
            onError = { reportedFailure = it },
            onResult = { resultInvoked = true },
        )

        assertEquals(expected = failure, actual = reportedFailure)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogLauncher_propagatesCoroutineCancellation_withoutInvokingCallbacks() = runTest {
        var errorInvoked = false
        var resultInvoked = false

        assertFailsWith<CancellationException> {
            runDialogLauncher(
                openDialog = { throw CancellationException("cancelled") },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogLauncher_unexpectedExceptionPropagates_withoutInvokingCallbacks() = runTest {
        val unexpectedFailure = IllegalStateException("Unexpected failure")
        var errorInvoked = false
        var resultInvoked = false

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogLauncher(
                openDialog = { throw unexpectedFailure },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertSame(unexpectedFailure, thrown)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogLauncher_resultCallbackExceptionPropagates_withoutInvokingError() = runTest {
        val callbackFailure = IllegalStateException("Result callback failed")
        var errorInvoked = false

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogLauncher(
                openDialog = { "picked" },
                onError = { errorInvoked = true },
                onResult = { throw callbackFailure },
            )
        }

        assertSame(callbackFailure, thrown)
        assertFalse(errorInvoked)
    }

    @Test
    fun runDialogLauncher_errorCallbackExceptionPropagatesOnce() = runTest {
        val callbackFailure = IllegalStateException("Error callback failed")
        var errorCalls = 0

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogLauncher(
                openDialog = { throw FileKitDialogException("Dialog failed") },
                onError = {
                    errorCalls++
                    throw callbackFailure
                },
                onResult = { error("Result must not be invoked") },
            )
        }

        assertSame(callbackFailure, thrown)
        assertEquals(1, errorCalls)
    }

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

private fun <Result> assertLegacyResultFailureReportsNull() {
    var callbackInvoked = false
    var result: Result? = null

    legacyNullResultOnDialogFailure<Result> {
        callbackInvoked = true
        result = it
    }.invoke(FileKitDialogException("Dialog failed"))

    assertTrue(callbackInvoked)
    assertEquals(null, result)
}
