@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import kotlin.test.Test
import kotlin.test.assertFailsWith

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

        pendingState.begin()
        pendingState.finish()
        pendingState.begin()
    }
}
