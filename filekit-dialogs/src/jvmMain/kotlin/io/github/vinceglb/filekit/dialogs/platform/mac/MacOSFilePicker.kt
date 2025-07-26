package io.github.vinceglb.filekit.dialogs.platform.mac

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMacOSSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.Foundation
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.ID
import io.github.vinceglb.filekit.path
import java.io.File

internal class MacOSFilePicker : PlatformFilePicker {
    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.SingleFile,
            directory = directory,
            fileExtensions = fileExtensions,
            title = title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? {
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.MultipleFiles,
            directory = directory,
            fileExtensions = fileExtensions,
            title = title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    override suspend fun openDirectoryPicker(
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        return callNativeMacOSPicker(
            mode = MacOSFilePickerMode.Directories,
            directory = directory,
            fileExtensions = null,
            title = title,
            macOSSettings = dialogSettings.macOS,
        )
    }

    private fun <T> callNativeMacOSPicker(
        mode: MacOSFilePickerMode<T>,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        title: String?,
        macOSSettings: FileKitMacOSSettings,
    ): T? {
        val pool = Foundation.NSAutoreleasePool()
        return try {
            var response: T? = null

            Foundation.executeOnMainThread(
                withAutoreleasePool = false,
                waitUntilDone = true,
            ) {
                // Create the file picker
                val openPanel = Foundation.invoke("NSOpenPanel", "new")

                // Setup single, multiple selection or directory mode
                mode.setupPickerMode(openPanel, macOSSettings.canCreateDirectories)

                // Set the title
                title?.let {
                    Foundation.invoke(openPanel, "setMessage:", Foundation.nsString(it))
                }

                // Set initial directory
                directory?.let {
                    Foundation.invoke(openPanel, "setDirectoryURL:", Foundation.nsURL(it.path))
                }

                // Set file extensions
                fileExtensions?.let { extensions ->
                    val items = extensions.map { Foundation.nsString(it) }
                    val nsData = Foundation.invokeVarArg(
                        "NSArray",
                        "arrayWithObjects:",
                        *items.toTypedArray(),
                    )
                    Foundation.invoke(openPanel, "setAllowedFileTypes:", nsData)
                }

                // Set resolvesAliases
                macOSSettings.resolvesAliases?.let { resolvesAliases ->
                    Foundation.invoke(openPanel, "setResolvesAliases:", resolvesAliases)
                }

                // Set window size
                println("Setting window size: ${macOSSettings.windowSize}")
                macOSSettings.windowSize?.let { (width, height) ->
                    // Create NSSize structure and set content size
                    val nsSize = Foundation.NSSize(width.toDouble(), height.toDouble())
                    Foundation.invoke(openPanel, "setContentSize:", nsSize)
                }

                // Open the file picker
                val result = Foundation.invoke(openPanel, "runModal")

                // Get the path(s) from the file picker if the user validated the selection
                if (result.toInt() == 1) {
                    response = mode.getResult(openPanel)
                }
            }

            response
        } finally {
            pool.drain()
        }
    }

    private companion object {
        fun singlePath(openPanel: ID): File? {
            val url = Foundation.invoke(openPanel, "URL")
            val nsPath = Foundation.invoke(url, "path")
            val path = Foundation.toStringViaUTF8(nsPath)
            return path?.let { File(it) }
        }

        fun multiplePaths(openPanel: ID): List<File>? {
            val urls = Foundation.invoke(openPanel, "URLs")
            val urlCount = Foundation.invoke(urls, "count").toInt()

            return (0 until urlCount).mapNotNull { index ->
                val url = Foundation.invoke(urls, "objectAtIndex:", index)
                val nsPath = Foundation.invoke(url, "path")
                val path = Foundation.toStringViaUTF8(nsPath)
                path?.let { File(it) }
            }.ifEmpty { null }
        }
    }

    private sealed class MacOSFilePickerMode<T> {
        abstract fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean)
        abstract fun getResult(openPanel: ID): T?

        data object SingleFile : MacOSFilePickerMode<File?>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", true)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
            }

            override fun getResult(openPanel: ID): File? = singlePath(openPanel)
        }

        data object MultipleFiles : MacOSFilePickerMode<List<File>>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", true)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", false)
                Foundation.invoke(openPanel, "setAllowsMultipleSelection:", true)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
                // MaxItems is not supported by MacOSFilePicker
            }

            override fun getResult(openPanel: ID): List<File>? = multiplePaths(openPanel)
        }

        data object Directories : MacOSFilePickerMode<File>() {
            override fun setupPickerMode(openPanel: ID, canCreateDirectories: Boolean) {
                Foundation.invoke(openPanel, "setCanChooseFiles:", false)
                Foundation.invoke(openPanel, "setCanChooseDirectories:", true)
                Foundation.invoke(openPanel, "setCanCreateDirectories:", canCreateDirectories)
            }

            override fun getResult(openPanel: ID): File? = singlePath(openPanel)
        }
    }
}
