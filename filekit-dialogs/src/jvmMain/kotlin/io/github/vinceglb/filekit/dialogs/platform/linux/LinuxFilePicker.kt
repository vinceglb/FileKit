package io.github.vinceglb.filekit.dialogs.platform.linux

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.dialogs.platform.swing.SwingFilePicker
import io.github.vinceglb.filekit.dialogs.platform.xdg.XdgFilePickerPortal
import java.io.File

/**
 * Delegate file picker for Linux platform choosing between [XdgFilePickerPortal], [AwtFilePicker] and [SwingFilePicker]
 * depending on what is available
 * [XdgFilePickerPortal] is checked first, if unavailable then [AwtFilePicker] is used for [pickFile] and [pickFiles] and
 * [SwingFilePicker] is used for [pickDirectory] because [AwtFilePicker] doesn't support picking directories
 */
internal class LinuxFilePicker(
    private val xdgFilePickerPortal: XdgFilePickerPortal,
    private val awtFilePicker: AwtFilePicker,
    private val swingFilePicker: SwingFilePicker,
) : PlatformFilePicker {

    private val xdgFilePickerPortalAvailable by lazy { xdgFilePickerPortal.isAvailable() }

    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickFile(
        initialDirectory,
        fileExtensions,
        title,
        platformSettings
    ) else awtFilePicker.pickFile(initialDirectory, fileExtensions, title, platformSettings)

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): List<File>? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickFiles(
        initialDirectory,
        fileExtensions,
        title,
        platformSettings
    ) else awtFilePicker.pickFiles(initialDirectory, fileExtensions, title, platformSettings)

    override suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File? =
        if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickDirectory(
            initialDirectory,
            title,
            platformSettings
        ) else swingFilePicker.pickDirectory(initialDirectory, title, platformSettings)

    override suspend fun saveFile(
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.saveFile(
        baseName, extension, initialDirectory, platformSettings
    ) else awtFilePicker.saveFile(baseName, extension, initialDirectory, platformSettings)
}