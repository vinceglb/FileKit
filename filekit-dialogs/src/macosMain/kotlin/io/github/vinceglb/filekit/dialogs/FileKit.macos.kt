package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import platform.AppKit.NSModalResponseOK
import platform.AppKit.NSOpenPanel
import platform.AppKit.NSSavePanel
import platform.AppKit.allowedFileTypes
import platform.Foundation.NSURL

public actual suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType,
    mode: FileKitMode<Out>,
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): Out? = callPicker(
    mode = when (mode) {
        is FileKitMode.Single -> Mode.Single
        is FileKitMode.Multiple -> Mode.Multiple
    },
    title = title,
    initialDirectory = initialDirectory,
    fileExtensions = when (type) {
        FileKitType.Image -> imageExtensions
        FileKitType.Video -> videoExtensions
        FileKitType.ImageAndVideo -> imageExtensions + videoExtensions
        is FileKitType.File -> type.extensions
    },
    platformSettings = platformSettings,
)?.map { PlatformFile(it) }?.let { mode.parseResult(it) }

public actual suspend fun FileKit.openDirectoryPicker(
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): PlatformFile? = callPicker(
    mode = Mode.Directory,
    title = title,
    initialDirectory = initialDirectory,
    fileExtensions = null,
    platformSettings = platformSettings,
)?.firstOrNull()?.let { PlatformFile(it) }

public actual suspend fun FileKit.openFileSaver(
    baseName: String,
    extension: String,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): PlatformFile? {
    // Create an NSSavePanel
    val nsSavePanel = NSSavePanel()

    // Set the initial directory
    initialDirectory?.let { nsSavePanel.directoryURL = NSURL.fileURLWithPath(it) }

    // Set the file name
    nsSavePanel.nameFieldStringValue = "$baseName.$extension"
    nsSavePanel.allowedFileTypes = listOf(extension)

    // Accept the creation of directories
    nsSavePanel.canCreateDirectories = platformSettings.canCreateDirectories

    // Run the NSSavePanel
    val result = nsSavePanel.runModal()

    // If the user cancelled the operation, return null
    if (result != NSModalResponseOK) {
        return null
    }

    // Return the result
    val platformFile = nsSavePanel.URL?.let { nsUrl ->
        // Write the bytes to the file
        // writeBytesArrayToNsUrl(bytes, nsUrl)

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
    platformSettings: FileKitDialogSettings,
): List<NSURL>? {
    // Create an NSOpenPanel
    val nsOpenPanel = NSOpenPanel()

    // Configure the NSOpenPanel
    nsOpenPanel.configure(
        mode = mode,
        title = title,
        extensions = fileExtensions,
        initialDirectory = initialDirectory,
        canCreateDirectories = platformSettings.canCreateDirectories
    )

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
    canCreateDirectories: Boolean,
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

    // Accept the creation of directories
    this.canCreateDirectories = canCreateDirectories

    return this
}

private enum class Mode {
    Single,
    Multiple,
    Directory
}
