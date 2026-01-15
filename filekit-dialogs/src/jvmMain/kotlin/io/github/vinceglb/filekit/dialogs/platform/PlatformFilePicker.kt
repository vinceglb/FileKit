package io.github.vinceglb.filekit.dialogs.platform

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.dialogs.platform.awt.AwtFileSaver
import io.github.vinceglb.filekit.dialogs.platform.linux.LinuxFilePicker
import io.github.vinceglb.filekit.dialogs.platform.mac.MacOSFilePicker
import io.github.vinceglb.filekit.dialogs.platform.swing.SwingFilePicker
import io.github.vinceglb.filekit.dialogs.platform.windows.WindowsFilePicker
import io.github.vinceglb.filekit.dialogs.platform.xdg.XdgFilePickerPortal
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import java.io.File

internal interface PlatformFilePicker {
    suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File?

    suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>?

    suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File?

    suspend fun openFileSaver(
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = AwtFileSaver.saveFile(
        suggestedName = suggestedName,
        extension = extension,
        directory = directory,
        dialogSettings = dialogSettings,
    )

    companion object {
        val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

        private fun createPlatformFilePicker(): PlatformFilePicker = when (PlatformUtil.current) {
            Platform.MacOS -> MacOSFilePicker()
            Platform.Windows -> WindowsFilePicker()
            Platform.Linux -> LinuxFilePicker(XdgFilePickerPortal(), AwtFilePicker(), SwingFilePicker())
        }
    }
}
