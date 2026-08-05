@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class FileKitDialogSettingsTest {
    @Test
    fun FileKitDialogSettings_defaultValues_hasNoParent() {
        assertNull(FileKitDialogSettings().parent)
        assertNull(FileKitDialogSettings.createDefault().parent)
    }

    @Test
    fun FileKitDialogSettings_withNativeParent_hasDataClassValueBehavior() {
        val macOS = FileKitMacOSSettings()
        val parent = FileKitDialogParent.windows(42)
        val first = FileKitDialogSettings(title = "Choose", parent = parent, macOS = macOS)
        val second = FileKitDialogSettings(title = "Choose", parent = parent, macOS = macOS)

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertSame(parent, first.component2())
    }

    @Test
    fun FileKitDialogSettings_copyingOtherValues_preservesOrClearsParent() {
        val parent = FileKitDialogParent.x11(42)
        val settings = FileKitDialogSettings(parent = parent)

        assertSame(parent, settings.copy(title = "Choose").parent)
        assertNull(settings.copy(parent = null).parent)
    }

    @Test
    fun FileKitDialogSettings_toString_redactsNativeParentValue() {
        val rendered = FileKitDialogSettings(parent = FileKitDialogParent.windows(0x1234)).toString()

        assertEquals(true, rendered.contains("FileKitDialogParent.Windows"))
        assertEquals(false, rendered.contains("1234"))
    }
}
