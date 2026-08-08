@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.windows

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class WindowsFileSaverFailureTest {
    @Test
    fun WindowsFileSaver_comInitializationFailure_throwsDialogOperationalFailureWithCause() = runTest {
        val executor = WindowsDialogExecutor(
            comRuntime = object : WindowsComRuntime {
                override fun initializeSta(): Int = 0x8007000E.toInt()

                override fun uninitialize() = Unit
            },
        )

        try {
            val failure = assertFailsWith<FileKitDialogException> {
                WindowsFilePicker(executor).openFileSaver(
                    suggestedName = "document",
                    defaultExtension = "txt",
                    allowedExtensions = setOf("txt"),
                    directory = null,
                    dialogSettings = FileKitDialogSettings(),
                )
            }

            assertIs<WindowsDialogOperationalException>(failure.cause)
        } finally {
            executor.close()
        }
    }
}
