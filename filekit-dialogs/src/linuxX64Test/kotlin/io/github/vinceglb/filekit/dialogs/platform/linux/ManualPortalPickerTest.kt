@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.platform.linux

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix.getenv
import kotlin.test.Test

/**
 * Manual integration test that opens the real GNOME file picker dialog through the XDG desktop
 * portal. It is skipped by default and only runs when the `FILEKIT_MANUAL_PICKER_TEST` environment
 * variable is set to `1`, so it never blocks the regular test suite.
 */
class ManualPortalPickerTest {
    @Test
    fun ManualPortalPicker_openFilePickerWithRealPortal_printsSelectedPaths() {
        if (!manualPickerTestEnabled()) {
            println("SKIP: set FILEKIT_MANUAL_PICKER_TEST=1 to open the real file picker dialog")
            return
        }

        println(">>> Opening the GNOME file picker dialog, please select one or more files...")
        val files = runBlocking {
            FileKit.openFilePicker(
                type = FileKitType.File(),
                mode = FileKitMode.Multiple(),
                directory = null,
                dialogSettings = FileKitDialogSettings(title = "FileKit manual picker test"),
            )
        }

        if (files.isNullOrEmpty()) {
            println(">>> No file selected (dialog cancelled)")
        } else {
            println(">>> Selected ${files.size} file(s):")
            files.forEach { file ->
                println("SELECTED: ${file.path}")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun manualPickerTestEnabled(): Boolean =
        getenv("FILEKIT_MANUAL_PICKER_TEST")?.toKString() == "1"
}
