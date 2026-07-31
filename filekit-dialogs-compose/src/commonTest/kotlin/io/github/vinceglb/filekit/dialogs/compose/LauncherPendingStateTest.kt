@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitMode
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LauncherPendingStateTest {
    @Test
    fun LauncherPendingState_secondBeginBeforeFinish_throwsProgrammerError() {
        val pendingState = LauncherPendingState("directory picker")

        pendingState.begin()

        assertFailsWith<IllegalStateException> { pendingState.begin() }
    }

    @Test
    fun LauncherPendingState_beginAfterFinish_succeeds() {
        val pendingState = LauncherPendingState("directory picker")

        val launch = pendingState.begin()
        pendingState.finish(launch)
        pendingState.begin()
    }

    @Test
    fun SinglePendingDialog_resultCallback_canBeginNextLaunch() = runTest {
        val pendingState = LauncherPendingState("directory picker")
        var retryFailure: Throwable? = null

        launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
            runDialogLauncher(
                openDialog = { "result" },
                beforeCallback = finishPendingLaunch,
                onError = {},
                onResult = {
                    retryFailure = runCatching { pendingState.begin() }.exceptionOrNull()
                },
            )
        }
        yield()

        assertNull(retryFailure)
    }

    @Test
    fun SinglePendingDialog_errorCallback_canBeginNextLaunch() = runTest {
        val pendingState = LauncherPendingState("directory picker")
        var retryFailure: Throwable? = null

        launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
            runDialogLauncher(
                openDialog = { throw FileKitDialogException("launch failed") },
                beforeCallback = finishPendingLaunch,
                onError = {
                    retryFailure = runCatching { pendingState.begin() }.exceptionOrNull()
                },
                onResult = {},
            )
        }
        yield()

        assertNull(retryFailure)
    }

    @Test
    fun SinglePendingFilePicker_resultCallback_canBeginNextLaunch() = runTest {
        val pendingState = LauncherPendingState("file picker")
        var retryFailure: Throwable? = null

        launchSinglePendingDialog(pendingState) { finishPendingLaunch ->
            runFilePickerLauncher(
                mode = FileKitMode.Single,
                openPicker = { null },
                beforeCallback = finishPendingLaunch,
                onError = {},
                onResult = {
                    retryFailure = runCatching { pendingState.begin() }.exceptionOrNull()
                },
            )
        }
        yield()

        assertNull(retryFailure)
    }

    @Test
    fun LauncherPendingState_staleFinish_doesNotClearNewLaunch() {
        val pendingState = LauncherPendingState("directory picker")
        val firstLaunch = pendingState.begin()
        pendingState.finish(firstLaunch)
        pendingState.begin()

        pendingState.finish(firstLaunch)

        assertFailsWith<IllegalStateException> { pendingState.begin() }
    }
}
