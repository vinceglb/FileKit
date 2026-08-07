@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AwtFilePickerFailureTest {
    @Test
    fun AwtFilePicker_headlessFailure_throwsPickerOperationalFailureWithCause() = runTest {
        assumeTrue(System.getProperty("filekit.test.headlessAwtFilePicker") == "true")
        check(GraphicsEnvironment.isHeadless())

        val failure = assertFailsWith<FileKitPickerException> {
            AwtFilePicker().openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertIs<HeadlessException>(failure.cause)
    }
}
