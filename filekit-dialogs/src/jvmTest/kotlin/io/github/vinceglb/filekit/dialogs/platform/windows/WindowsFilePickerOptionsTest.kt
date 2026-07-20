@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs.platform.windows

import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.FILEOPENDIALOGOPTIONS.Companion.FOS_ALLOWMULTISELECT
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.FILEOPENDIALOGOPTIONS.Companion.FOS_FORCEFILESYSTEM
import kotlin.test.Test
import kotlin.test.assertEquals

class WindowsFilePickerOptionsTest {
    @Test
    fun requiredFileDialogOptions_whenOptionsAlreadyExist_preservesOptionsAndForcesFilesystemResults() {
        assertEquals(
            expected = FOS_ALLOWMULTISELECT or FOS_FORCEFILESYSTEM,
            actual = requiredFileDialogOptions(FOS_ALLOWMULTISELECT),
        )
    }
}
