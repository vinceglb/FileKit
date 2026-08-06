@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.linux

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.dialogs.platform.swing.SwingFilePicker
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinuxFilePickerTest {
    @Test
    fun LinuxFilePicker_withAvailablePortal_routesEveryOperationToPortal() = runTest {
        val portal = RecordingFilePicker("portal")
        val awt = RecordingFilePicker("awt")
        val swing = RecordingFilePicker("swing")
        var availabilityChecks = 0
        val picker = LinuxFilePicker(portal, awt, swing) {
            availabilityChecks += 1
            true
        }

        callEveryOperation(picker)

        assertEquals(listOf("open", "multi", "directory", "save"), portal.operations)
        assertEquals(emptyList(), awt.operations)
        assertEquals(emptyList(), swing.operations)
        assertEquals(1, availabilityChecks)
    }

    @Test
    fun LinuxFilePicker_withoutAvailablePortal_routesToAwtAndSwingFallbacks() = runTest {
        val portal = RecordingFilePicker("portal")
        val awt = RecordingFilePicker("awt")
        val swing = RecordingFilePicker("swing")
        val picker = LinuxFilePicker(portal, awt, swing) { false }

        callEveryOperation(picker)

        assertEquals(emptyList(), portal.operations)
        assertEquals(listOf("open", "multi", "save"), awt.operations)
        assertEquals(listOf("directory"), swing.operations)
    }

    @Test
    fun LinuxFilePicker_withoutAvailablePortal_rejectsNativeParentsOnEveryFallback() = runTest {
        val picker = LinuxFilePicker(
            xdgFilePickerPortal = RecordingFilePicker("portal"),
            awtFilePicker = AwtFilePicker(),
            swingFilePicker = SwingFilePicker(),
            isXdgFilePickerPortalAvailable = { false },
        )
        val settings = FileKitDialogSettings(parent = FileKitDialogParent.x11(42))

        assertFailsWith<IllegalArgumentException> {
            picker.openFilePicker(null, null, settings)
        }
        assertFailsWith<IllegalArgumentException> {
            picker.openFilesPicker(null, null, settings)
        }
        assertFailsWith<IllegalArgumentException> {
            picker.openDirectoryPicker(null, settings)
        }
        assertFailsWith<IllegalArgumentException> {
            picker.openFileSaver("example", "txt", null, null, settings)
        }
    }

    private suspend fun callEveryOperation(picker: PlatformFilePicker) {
        val settings = FileKitDialogSettings()
        picker.openFilePicker(null, null, settings)
        picker.openFilesPicker(null, null, settings)
        picker.openDirectoryPicker(null, settings)
        picker.openFileSaver("example", "txt", null, null, settings)
    }
}

private class RecordingFilePicker(
    private val name: String,
) : PlatformFilePicker {
    val operations = mutableListOf<String>()

    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        operations += "open"
        return null
    }

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? {
        operations += "multi"
        return null
    }

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        operations += "directory"
        return null
    }

    override suspend fun openFileSaver(
        suggestedName: String,
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        operations += "save"
        return null
    }

    override fun toString(): String = name
}
