@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

class FileSaverLauncherJvmTest {
    @Test
    fun runFileSaverLauncher_success_invokesDestinationOnce_withoutInvokingError() = runTest {
        val destination = PlatformFile(File("saved-document.txt"))
        val results = mutableListOf<PlatformFile?>()
        var errorInvoked = false

        runFileSaverLauncher(
            openFileSaver = { destination },
            onError = { errorInvoked = true },
            onResult = results::add,
        )

        assertSame(destination, results.single())
        assertFalse(errorInvoked)
    }
}
