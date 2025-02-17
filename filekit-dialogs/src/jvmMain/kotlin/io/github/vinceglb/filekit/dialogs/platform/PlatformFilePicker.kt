package io.github.vinceglb.filekit.dialogs.platform

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
    suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File?

    suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): List<File>?

    suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File?

    suspend fun saveFile(
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitDialogSettings,
    ): File? = AwtFileSaver.saveFile(
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )

    companion object {
        val current: PlatformFilePicker by lazy { createPlatformFilePicker() }

        private fun createPlatformFilePicker(): PlatformFilePicker {
            return when (PlatformUtil.current) {
                Platform.MacOS -> MacOSFilePicker()
                Platform.Windows -> WindowsFilePicker()
                Platform.Linux -> LinuxFilePicker(XdgFilePickerPortal(), AwtFilePicker(), SwingFilePicker())
            }
        }
    }
}
