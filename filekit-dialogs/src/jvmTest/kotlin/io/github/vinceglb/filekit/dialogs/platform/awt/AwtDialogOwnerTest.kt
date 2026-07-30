@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import kotlinx.coroutines.test.runTest
import java.awt.Dialog
import java.awt.Frame
import java.awt.Window
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AwtDialogOwnerTest {
    @Test
    fun isSupportedAwtFileDialogOwner_withOwnerClasses_matchesFileDialogConstructors() {
        assertTrue(isSupportedAwtFileDialogOwner(Frame::class.java))
        assertTrue(isSupportedAwtFileDialogOwner(Dialog::class.java))
        assertFalse(isSupportedAwtFileDialogOwner(Window::class.java))
    }

    @Test
    fun AwtFilePicker_withNativeParent_failsBeforeOpeningUi() = runTest {
        assertFailsWith<FileKitPickerException> {
            AwtFilePicker().openFilePicker(
                fileExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(
                    parent = FileKitDialogParent.windows(42),
                ),
            )
        }
    }

    @Test
    fun AwtFileSaver_withNativeParent_failsBeforeOpeningUi() = runTest {
        assertFailsWith<FileKitPickerException> {
            AwtFileSaver.saveFile(
                suggestedName = "example",
                defaultExtension = "txt",
                allowedExtensions = null,
                directory = null,
                dialogSettings = FileKitDialogSettings(
                    parent = FileKitDialogParent.x11(42),
                ),
            )
        }
    }
}
