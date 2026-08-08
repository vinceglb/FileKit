@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AwtDirectoryPickerFailureTest {
    @Test
    fun AwtDirectoryPicker_unsupportedValidRequest_throwsDialogOperationalFailure() = runTest {
        val failure = assertFailsWith<FileKitDialogException> {
            AwtFilePicker().openDirectoryPicker(
                directory = null,
                dialogSettings = FileKitDialogSettings(),
            )
        }

        assertEquals("AWT does not support directory picker dialogs.", failure.message)
    }
}
