package io.github.vinceglb.filekit.core.platform.linux

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.awt.AwtFilePicker
import io.github.vinceglb.filekit.core.platform.swing.SwingFilePicker
import io.github.vinceglb.filekit.core.platform.xdg.XdgFilePickerPortal
import java.awt.Window
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
        parentWindow: Window?
    ): File? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickFile(
        initialDirectory,
        fileExtensions,
        title,
        parentWindow
    ) else awtFilePicker.pickFile(initialDirectory, fileExtensions, title, parentWindow)

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?
    ): List<File>? = if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickFiles(
        initialDirectory,
        fileExtensions,
        title,
        parentWindow
    ) else awtFilePicker.pickFiles(initialDirectory, fileExtensions, title, parentWindow)

    override suspend fun pickDirectory(initialDirectory: String?, title: String?, parentWindow: Window?): File? =
        if (xdgFilePickerPortalAvailable) xdgFilePickerPortal.pickDirectory(
            initialDirectory,
            title,
            parentWindow
        ) else swingFilePicker.pickDirectory(initialDirectory, title, parentWindow)
}