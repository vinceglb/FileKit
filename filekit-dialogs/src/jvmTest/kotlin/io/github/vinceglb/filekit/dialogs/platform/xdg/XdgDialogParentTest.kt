package io.github.vinceglb.filekit.dialogs.platform.xdg

import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import kotlin.test.Test
import kotlin.test.assertEquals

class XdgDialogParentTest {
    @Test
    fun xdgFileChooserParent_returnsLowercaseX11IdentifierForOpenAndSave() {
        val parent = FileKitDialogParent.x11(0x1a2b)

        assertEquals("x11:1a2b", xdgFileChooserParent(parent))
    }

    @Test
    fun xdgFileChooserParent_returnsOpaqueWaylandIdentifierForOpenAndSave() {
        val parent = FileKitDialogParent.wayland("xdg-foreign-token")

        assertEquals("wayland:xdg-foreign-token", xdgFileChooserParent(parent))
    }
}
