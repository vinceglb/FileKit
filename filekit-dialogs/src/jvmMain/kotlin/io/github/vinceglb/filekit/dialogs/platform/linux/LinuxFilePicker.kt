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
 * [XdgFilePickerPortal] is checked first, if unavailable then [AwtFilePicker] is used for [openFilePicker] and [openFilesPicker] and
 * [SwingFilePicker] is used for [openDirectoryPicker] because [AwtFilePicker] doesn't support picking directories
 */
internal class LinuxFilePicker(
    private val xdgFilePickerPortal: XdgFilePickerPortal,
    private val awtFilePicker: AwtFilePicker,
    private val swingFilePicker: SwingFilePicker,
) : PlatformFilePicker {

    private val xdgFilePickerPortalAvailable by lazy { xdgFilePickerPortal.isAvailable() }

    override suspend fun openFilePicker(
        initialDirectory: String?,
        fileExtensions: Set<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.openFilePicker(
        initialDirectory,
        fileExtensions,
        title,
        dialogSettings
    ) else awtFilePicker.openFilePicker(initialDirectory, fileExtensions, title, dialogSettings)

    override suspend fun openFilesPicker(
        initialDirectory: String?,
        fileExtensions: Set<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.openFilesPicker(
        initialDirectory,
        fileExtensions,
        title,
        dialogSettings
    ) else awtFilePicker.openFilesPicker(initialDirectory, fileExtensions, title, dialogSettings)

    override suspend fun openDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? =
        if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.openDirectoryPicker(
            initialDirectory,
            title,
            dialogSettings
        ) else swingFilePicker.openDirectoryPicker(initialDirectory, title, dialogSettings)

    override suspend fun openFileSaver(
        baseName: String,
        extension: String,
        initialDirectory: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.openFileSaver(
        baseName, extension, initialDirectory, dialogSettings
    ) else awtFilePicker.openFileSaver(baseName, extension, initialDirectory, dialogSettings)
}