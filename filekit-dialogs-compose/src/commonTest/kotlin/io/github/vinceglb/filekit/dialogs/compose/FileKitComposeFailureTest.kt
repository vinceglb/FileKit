@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FileKitComposeFailureTest {
    @Test
    fun runShareFileLauncher_operationalFailure_invokesErrorOnce() = runTest {
        val failure = FileKitDialogException("The share sheet could not be opened.")
        val reportedFailures = mutableListOf<FileKitDialogException>()

        runShareFileLauncher(
            shareFiles = { throw failure },
            onError = reportedFailures::add,
        )

        assertEquals(listOf(failure), reportedFailures)
    }

    @Test
    fun runShareFileLauncher_success_doesNotInvokeError() = runTest {
        var errorInvoked = false

        runShareFileLauncher(
            shareFiles = {},
            onError = { errorInvoked = true },
        )

        assertFalse(errorInvoked)
    }

    @Test
    fun runShareFileLauncher_legacyIgnoredFailure_invokesNoCallback() = runTest {
        runShareFileLauncher(
            shareFiles = { throw FileKitDialogException("Ignored compatibility failure") },
            onError = {},
        )
    }

    @Test
    fun runCameraPickerLauncher_operationalFailure_invokesErrorOnce_withoutInvokingResult() = runTest {
        val failure = FileKitDialogException("The camera could not be opened.")
        val reportedFailures = mutableListOf<FileKitDialogException>()
        var resultInvoked = false

        runCameraPickerLauncher(
            openCameraPicker = { throw failure },
            onError = reportedFailures::add,
            onResult = { resultInvoked = true },
        )

        assertEquals(listOf(failure), reportedFailures)
        assertFalse(resultInvoked)
    }

    @Test
    fun runCameraPickerLauncher_userCancellation_invokesNullResultOnce_withoutInvokingError() = runTest {
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runCameraPickerLauncher(
            openCameraPicker = { null },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(1, results.size)
        assertEquals(null, results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runCameraPickerLauncher_legacyIgnoredFailure_invokesNoResult() = runTest {
        var resultInvoked = false

        runCameraPickerLauncher(
            openCameraPicker = { throw FileKitDialogException("Ignored compatibility failure") },
            onError = {},
            onResult = { resultInvoked = true },
        )

        assertFalse(resultInvoked)
    }

    @Test
    fun runFileSaverLauncher_operationalFailure_invokesErrorOnce_withoutInvokingResult() = runTest {
        val failure = FileKitDialogException("The file saver could not be opened.")
        val reportedFailures = mutableListOf<FileKitDialogException>()
        var resultInvoked = false

        runFileSaverLauncher(
            openFileSaver = { throw failure },
            onError = reportedFailures::add,
            onResult = { resultInvoked = true },
        )

        assertEquals(listOf(failure), reportedFailures)
        assertFalse(resultInvoked)
    }

    @Test
    fun runFileSaverLauncher_userCancellation_invokesNullResultOnce_withoutInvokingError() = runTest {
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runFileSaverLauncher(
            openFileSaver = { null },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(1, results.size)
        assertEquals(null, results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runFileSaverLauncher_invalidInvocation_propagates_withoutInvokingCallbacks() = runTest {
        val failure = IllegalArgumentException("Unsupported saver arguments")
        var errorInvoked = false
        var resultInvoked = false

        val thrown = assertFailsWith<IllegalArgumentException> {
            runFileSaverLauncher(
                openFileSaver = { throw failure },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertSame(failure, thrown)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runFileSaverLauncher_legacyIgnoredFailure_invokesNoResult() = runTest {
        var resultInvoked = false

        runFileSaverLauncher(
            openFileSaver = { throw FileKitDialogException("Ignored compatibility failure") },
            onError = {},
            onResult = { resultInvoked = true },
        )

        assertFalse(resultInvoked)
    }

    @Test
    fun runDirectoryPickerLauncher_operationalFailure_invokesErrorOnce_withoutInvokingResult() = runTest {
        val failure = FileKitDialogException("The directory picker could not be opened.")
        val reportedFailures = mutableListOf<FileKitDialogException>()
        var resultInvoked = false

        runDirectoryPickerLauncher(
            openDirectoryPicker = { throw failure },
            onError = reportedFailures::add,
            onResult = { resultInvoked = true },
        )

        assertEquals(listOf(failure), reportedFailures)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDirectoryPickerLauncher_userCancellation_invokesNullResultOnce_withoutInvokingError() = runTest {
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runDirectoryPickerLauncher(
            openDirectoryPicker = { null },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(1, results.size)
        assertEquals(null, results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runDirectoryPickerLauncher_invalidInvocation_propagates_withoutInvokingCallbacks() = runTest {
        val failure = IllegalArgumentException("Unsupported directory argument")
        var errorInvoked = false
        var resultInvoked = false

        val thrown = assertFailsWith<IllegalArgumentException> {
            runDirectoryPickerLauncher(
                openDirectoryPicker = { throw failure },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertSame(failure, thrown)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDirectoryPickerLauncher_legacyIgnoredFailure_invokesNoResult() = runTest {
        var resultInvoked = false

        runDirectoryPickerLauncher(
            openDirectoryPicker = { throw FileKitDialogException("Ignored compatibility failure") },
            onError = {},
            onResult = { resultInvoked = true },
        )

        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_operationalFailure_invokesErrorOnce_withoutInvokingResult() = runTest {
        val failure = FileKitDialogException("The system dialog could not be opened.")
        val reportedFailures = mutableListOf<FileKitDialogException>()
        var resultInvoked = false

        runDialogOperation(
            operation = { throw failure },
            onError = reportedFailures::add,
            onResult = { resultInvoked = true },
        )

        assertEquals(listOf(failure), reportedFailures)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_success_invokesResultOnce_withoutInvokingError() = runTest {
        val results = mutableListOf<String?>()
        var errorInvoked = false

        runDialogOperation(
            operation = { null },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(1, results.size)
        assertEquals(null, results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runDialogOperation_coroutineCancellation_propagates_withoutInvokingCallbacks() = runTest {
        var errorInvoked = false
        var resultInvoked = false

        assertFailsWith<CancellationException> {
            runDialogOperation(
                operation = { throw CancellationException("Cancelled by caller") },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_cancelledJobAfterNonCooperativeSuccess_invokesNoCallbacks() = runTest {
        lateinit var completeOperation: (Result<String>) -> Unit
        var errorInvoked = false
        var resultInvoked = false

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            runDialogOperation(
                operation = {
                    suspendCoroutine { continuation ->
                        completeOperation = continuation::resumeWith
                    }
                },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        job.cancel()
        completeOperation(Result.success("selected"))
        job.join()

        assertTrue(job.isCancelled)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_cancelledJobAfterNonCooperativeFailure_invokesNoCallbacks() = runTest {
        lateinit var completeOperation: (Result<String>) -> Unit
        var errorInvoked = false
        var resultInvoked = false

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            runDialogOperation(
                operation = {
                    suspendCoroutine { continuation ->
                        completeOperation = continuation::resumeWith
                    }
                },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        job.cancel()
        completeOperation(Result.failure(FileKitDialogException("Late operational failure")))
        job.join()

        assertTrue(job.isCancelled)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_unexpectedFailure_propagates_withoutInvokingCallbacks() = runTest {
        val failure = IllegalStateException("Unexpected picker defect")
        var errorInvoked = false
        var resultInvoked = false

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogOperation(
                operation = { throw failure },
                onError = { errorInvoked = true },
                onResult = { resultInvoked = true },
            )
        }

        assertSame(failure, thrown)
        assertFalse(errorInvoked)
        assertFalse(resultInvoked)
    }

    @Test
    fun runDialogOperation_resultCallbackFailure_propagates_withoutInvokingError() = runTest {
        val failure = IllegalStateException("Consumer result callback failed")
        var errorInvoked = false

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogOperation(
                operation = { "selected" },
                onError = { errorInvoked = true },
                onResult = { throw failure },
            )
        }

        assertSame(failure, thrown)
        assertFalse(errorInvoked)
    }

    @Test
    fun runDialogOperation_errorCallbackFailure_propagates_once() = runTest {
        val callbackFailure = IllegalStateException("Consumer error callback failed")
        var errorInvocations = 0

        val thrown = assertFailsWith<IllegalStateException> {
            runDialogOperation(
                operation = { throw FileKitDialogException("Operational failure") },
                onError = {
                    errorInvocations++
                    throw callbackFailure
                },
                onResult = {},
            )
        }

        assertSame(callbackFailure, thrown)
        assertEquals(1, errorInvocations)
    }

    @Test
    fun runFilePickerLauncher_pickerFailure_invokesErrorOnce_withoutInvokingResult() = runTest {
        val failure = FileKitPickerException("Failed to load the selected file.")
        val reportedFailures = mutableListOf<FileKitPickerException>()
        var resultInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.Single,
            openPicker = { throw failure },
            onError = reportedFailures::add,
            onResult = { resultInvoked = true },
        )

        assertEquals(listOf(failure), reportedFailures)
        assertFalse(resultInvoked)
    }

    @Test
    fun runFilePickerLauncher_userCancellation_invokesResultOnce_withoutInvokingError() = runTest {
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.Single,
            openPicker = { null },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(1, results.size)
        assertEquals(null, results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runFilePickerLauncher_stateValueFailure_invokesResult_withoutInvokingError() = runTest {
        val failure = FileKitPickerException("Failed after selection.")
        val results = mutableListOf<FileKitPickerState<PlatformFile>>()
        var errorInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.SingleWithState,
            openPicker = { flowOf(FileKitPickerState.Failed(failure)) },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertEquals(FileKitPickerState.Failed(failure), results.single())
        assertFalse(errorInvoked)
    }

    @Test
    fun runFilePickerLauncher_thrownStateStreamFailure_reportsError_afterEarlierState() = runTest {
        val failure = FileKitPickerException("Failed while processing the selection.")
        val results = mutableListOf<FileKitPickerState<PlatformFile>>()
        val reportedFailures = mutableListOf<FileKitPickerException>()

        runFilePickerLauncher(
            mode = FileKitMode.SingleWithState,
            openPicker = {
                flow {
                    emit(FileKitPickerState.Started(total = 2))
                    throw failure
                }
            },
            onError = reportedFailures::add,
            onResult = results::add,
        )

        assertEquals(FileKitPickerState.Started(total = 2), results.single())
        assertEquals(listOf(failure), reportedFailures)
    }

    @Test
    fun runFilePickerLauncher_stateCallbackFailure_propagates_withoutInvokingError() = runTest {
        val callbackFailure = IllegalStateException("Consumer state callback failed")
        var errorInvoked = false

        val thrown = assertFailsWith<IllegalStateException> {
            runFilePickerLauncher(
                mode = FileKitMode.SingleWithState,
                openPicker = { flowOf(FileKitPickerState.Started(total = 1)) },
                onError = { errorInvoked = true },
                onResult = { throw callbackFailure },
            )
        }

        assertSame(callbackFailure, thrown)
        assertFalse(errorInvoked)
    }

    @Test
    fun runFilePickerLauncher_legacyIgnoredFailure_invokesNoResult() = runTest {
        var resultInvoked = false

        runFilePickerLauncher(
            mode = FileKitMode.Single,
            openPicker = { throw FileKitPickerException("Ignored compatibility failure") },
            onError = {},
            onResult = { resultInvoked = true },
        )

        assertFalse(resultInvoked)
    }
}
