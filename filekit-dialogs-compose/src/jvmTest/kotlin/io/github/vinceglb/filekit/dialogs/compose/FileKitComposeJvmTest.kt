@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class FileKitComposeJvmTest {
    @Test
    fun injectDialogSettings_withoutExistingSettings_usesScopeParent() {
        val scopeParent = FileKitDialogParent.windows(42)

        val result = injectDialogSettings(
            dialogSettings = null,
            parent = scopeParent,
        )

        assertSame(scopeParent, result.parent)
    }

    @Test
    fun injectDialogSettings_withExistingNativeParent_replacesItAndPreservesSettings() {
        val scopeParent = FileKitDialogParent.windows(42)
        val settings = FileKitDialogSettings(
            title = "Choose",
            parent = FileKitDialogParent.x11(7),
        )

        val result = injectDialogSettings(
            dialogSettings = settings,
            parent = scopeParent,
        )

        assertEquals("Choose", result.title)
        assertSame(scopeParent, result.parent)
    }
}
