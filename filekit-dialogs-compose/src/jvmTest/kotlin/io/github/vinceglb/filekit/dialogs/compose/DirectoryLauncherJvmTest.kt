@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

class DirectoryLauncherJvmTest {
    @Test
    fun runDirectoryPickerLauncher_success_invokesResultOnce_withoutInvokingError() = runTest {
        val directory = PlatformFile(File("selected-directory"))
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runDirectoryPickerLauncher(
            openDirectoryPicker = { directory },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertSame(directory, results.single())
        assertFalse(errorInvoked)
    }
}
