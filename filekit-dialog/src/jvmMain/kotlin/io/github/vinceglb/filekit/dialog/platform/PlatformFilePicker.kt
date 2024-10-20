package io.github.vinceglb.filekit.dialog.platform

import io.github.vinceglb.filekit.dialog.FileKitDialogSettings
import io.github.vinceglb.filekit.dialog.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.dialog.platform.awt.AwtFileSaver
import io.github.vinceglb.filekit.dialog.platform.linux.LinuxFilePicker
import io.github.vinceglb.filekit.dialog.platform.mac.MacOSFilePicker
import io.github.vinceglb.filekit.dialog.platform.swing.SwingFilePicker
import io.github.vinceglb.filekit.dialog.platform.windows.WindowsFilePicker
import io.github.vinceglb.filekit.dialog.platform.xdg.XdgFilePickerPortal
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import java.io.File

internal interface PlatformFilePicker {
    suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings?,
    ): File?

    suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings?,
    ): List<File>?

    suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        platformSettings: FileKitDialogSettings?,
    ): File?

    suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitDialogSettings?,
    ): File? = AwtFileSaver.saveFile(
        bytes = bytes,
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
