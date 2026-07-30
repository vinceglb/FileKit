@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.xdg

import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.test.runTest
import org.freedesktop.dbus.types.Variant
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class XdgDialogParentTest {
    @Test
    fun XdgFilePickerPortal_openAndSave_withX11Parent_forwardsCanonicalParent() = runTest {
        val transport = RecordingXdgFileChooserTransport()
        val picker = XdgFilePickerPortal(transport)
        val settings = FileKitDialogSettings(
            title = "Choose",
            parent = FileKitDialogParent.x11(0x2a),
        )

        picker.openFilePicker(
            fileExtensions = null,
            directory = null,
            dialogSettings = settings,
        )
        picker.openFileSaver(
            suggestedName = "example",
            defaultExtension = "txt",
            allowedExtensions = null,
            directory = null,
            dialogSettings = settings,
        )

        assertEquals("x11:2a", transport.openParent)
        assertEquals("x11:2a", transport.saveParent)
    }

    @Test
    fun XdgFilePickerPortal_open_withWaylandParent_forwardsOpaqueParent() = runTest {
        val transport = RecordingXdgFileChooserTransport()
        val picker = XdgFilePickerPortal(transport)

        picker.openFilePicker(
            fileExtensions = null,
            directory = null,
            dialogSettings = FileKitDialogSettings(
                parent = FileKitDialogParent.wayland("token with spaces"),
            ),
        )

        assertEquals("wayland:token with spaces", transport.openParent)
    }
}

private class RecordingXdgFileChooserTransport : XdgFileChooserTransport {
    var openParent: String? = null
    var saveParent: String? = null

    override fun isAvailable(): Boolean = true

    override suspend fun openFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? {
        openParent = parentWindow
        return null
    }

    override suspend fun saveFile(
        parentWindow: String,
        title: String,
        options: MutableMap<String, Variant<*>>,
    ): List<URI>? {
        saveParent = parentWindow
        return null
    }
}
