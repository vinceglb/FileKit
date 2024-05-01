package io.github.vinceglb.picker.core

import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSSavePanel
import platform.AppKit.allowedFileTypes
import platform.Foundation.NSURL

public actual object Picker {
    public actual suspend fun <Out> pick(
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?
    ): Out? {
        // Create an NSOpenPanel
        val nsOpenPanel = NSOpenPanel()

        // Configure the NSOpenPanel
        nsOpenPanel.configure(mode, title, initialDirectory)

        // Run the NSOpenPanel
        val result = nsOpenPanel.runModal()

        // If the user cancelled the operation, return null
        if (result != NSModalResponseOK) {
            return null
        }

        // Return the result
        val urls = nsOpenPanel.URLs.mapNotNull { it as? NSURL }
        val selection = PickerSelectionMode.SelectionResult(urls)
        return mode.result(selection)
    }

    public actual suspend fun save(
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

    private fun NSOpenPanel.configure(
        mode: PickerSelectionMode<*>,
        title: String?,
        initialDirectory: String?,
    ): NSOpenPanel {
        // Set the title
        title?.let { message = it }

        // Set the initial directory
        initialDirectory?.let { directoryURL = NSURL.fileURLWithPath(it) }

        // Setup the picker mode and files extensions
        when (mode) {
            is PickerSelectionMode.SingleFile -> {
                canChooseFiles = true
                canChooseDirectories = false
                allowsMultipleSelection = false

                // Set the allowed file types
                mode.extensions?.let { allowedFileTypes = mode.extensions }
            }

            is PickerSelectionMode.MultipleFiles -> {
                canChooseFiles = true
                canChooseDirectories = false
                allowsMultipleSelection = true

                // Set the allowed file types
                mode.extensions?.let { allowedFileTypes = mode.extensions }
            }

            is PickerSelectionMode.Directory -> {
                canChooseFiles = false
                canChooseDirectories = true
                allowsMultipleSelection = false
            }

            else -> throw IllegalArgumentException("Unsupported mode: $mode")
        }

        return this
    }
}
