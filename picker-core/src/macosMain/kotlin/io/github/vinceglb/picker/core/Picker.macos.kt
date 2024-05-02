package io.github.vinceglb.picker.core

import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSSavePanel
import platform.AppKit.allowedFileTypes
import platform.Foundation.NSURL

public actual object Picker {
    public actual suspend fun <Out> pickFile(
        type: PickerSelectionType,
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?,
    ): Out? = callPicker(
        mode = when (mode) {
            is PickerSelectionMode.Single -> Mode.Single
            is PickerSelectionMode.Multiple -> Mode.Multiple
        },
        title = title,
        initialDirectory = initialDirectory,
        fileExtensions = when (type) {
            PickerSelectionType.Image -> imageExtensions
            PickerSelectionType.Video -> videoExtensions
            PickerSelectionType.ImageAndVideo -> imageExtensions + videoExtensions
            is PickerSelectionType.File -> type.extensions
        },
    )?.map { PlatformFile(it) }?.let { mode.parseResult(it) }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?
    ): PlatformDirectory? = callPicker(
        mode = Mode.Directory,
        title = title,
        initialDirectory = initialDirectory,
        fileExtensions = null
    )?.firstOrNull()?.let { PlatformDirectory(it) }

    public actual fun isDirectoryPickerSupported(): Boolean = true

    public actual suspend fun saveFile(
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ): PlatformFile? {
        // Create an NSSavePanel
        val nsSavePanel = NSSavePanel()

        // Set the initial directory
        initialDirectory?.let { nsSavePanel.directoryURL = NSURL.fileURLWithPath(it) }

        // Set the file name
        nsSavePanel.nameFieldStringValue = "$baseName.$extension"
        nsSavePanel.allowedFileTypes = listOf(extension)

        // Accept the creation of directories
        nsSavePanel.canCreateDirectories = true

        // Run the NSSavePanel
        val result = nsSavePanel.runModal()

        // If the user cancelled the operation, return null
        if (result != NSModalResponseOK) {
            return null
        }

        // Return the result
        val platformFile = nsSavePanel.URL?.let { nsUrl ->
            // Write the bytes to the file
            writeBytesArrayToNsUrl(bytes, nsUrl)

            // Create the PlatformFile
            PlatformFile(nsUrl)
        }

        return platformFile
    }

    private fun callPicker(
        mode: Mode,
        title: String?,
        initialDirectory: String?,
        fileExtensions: List<String>?,
    ): List<NSURL>? {
        // Create an NSOpenPanel
        val nsOpenPanel = NSOpenPanel()

        // Configure the NSOpenPanel
        nsOpenPanel.configure(mode, title, fileExtensions, initialDirectory)

        // Run the NSOpenPanel
        val result = nsOpenPanel.runModal()

        // If the user cancelled the operation, return null
        if (result != NSModalResponseOK) {
            return null
        }

        // Return the result
        return nsOpenPanel.URLs.mapNotNull { it as? NSURL }
    }

    private fun NSOpenPanel.configure(
        mode: Mode,
        title: String?,
        extensions: List<String>?,
        initialDirectory: String?,
    ): NSOpenPanel {
        // Set the title
        title?.let { message = it }

        // Set the initial directory
        initialDirectory?.let { directoryURL = NSURL.fileURLWithPath(it) }

        // Set the allowed file types
        extensions?.let { allowedFileTypes = extensions }

        // Setup the picker mode and files extensions
        when (mode) {
            Mode.Single -> {
                canChooseFiles = true
                canChooseDirectories = false
                allowsMultipleSelection = false
            }

            Mode.Multiple -> {
                canChooseFiles = true
                canChooseDirectories = false
                allowsMultipleSelection = true
            }

            Mode.Directory -> {
                canChooseFiles = false
                canChooseDirectories = true
                allowsMultipleSelection = false
            }
        }

        return this
    }

    private enum class Mode {
        Single,
        Multiple,
        Directory
    }
}
