package io.github.vinceglb.filekit.dialogs.platform.linux

import io.github.vinceglb.filekit.PlatformFile
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
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) {
        xdgFilePickerPortal.openFilePicker(
            fileExtensions,
            directory,
            dialogSettings,
        )
    } else {
        awtFilePicker.openFilePicker(fileExtensions, directory, dialogSettings)
    }

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = if (xdgFilePickerPortalAvailable) {
        xdgFilePickerPortal.openFilesPicker(
            fileExtensions,
            directory,
            dialogSettings,
        )
    } else {
        awtFilePicker.openFilesPicker(fileExtensions, directory, dialogSettings)
    }

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? =
        if (xdgFilePickerPortalAvailable) {
            xdgFilePickerPortal.openDirectoryPicker(
                directory,
                dialogSettings,
            )
        } else {
            swingFilePicker.openDirectoryPicker(directory, dialogSettings)
        }

    override suspend fun openFileSaver(
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = if (xdgFilePickerPortalAvailable) {
        xdgFilePickerPortal.openFileSaver(
            suggestedName,
            extension,
            directory,
            dialogSettings,
        )
    } else {
        awtFilePicker.openFileSaver(suggestedName, extension, directory, dialogSettings)
    }
}
