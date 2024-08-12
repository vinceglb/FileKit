package io.github.vinceglb.filekit.core.platform

import io.github.vinceglb.filekit.core.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.core.platform.awt.AwtFileSaver
import io.github.vinceglb.filekit.core.platform.linux.LinuxFilePicker
import io.github.vinceglb.filekit.core.platform.mac.MacOSFilePicker
import io.github.vinceglb.filekit.core.platform.swing.SwingFilePicker
import io.github.vinceglb.filekit.core.platform.util.Platform
import io.github.vinceglb.filekit.core.platform.util.PlatformUtil
import io.github.vinceglb.filekit.core.platform.windows.WindowsFilePicker
import io.github.vinceglb.filekit.core.platform.xdg.XdgFilePickerPortal
import java.awt.Window
import java.io.File

internal interface PlatformFilePicker {
    suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?,
    ): File?

    suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?,
    ): List<File>?

    suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        parentWindow: Window?,
    ): File?

    suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        parentWindow: Window?,
    ): File? = AwtFileSaver.saveFile(
        bytes = bytes,
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        parentWindow = parentWindow,
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
