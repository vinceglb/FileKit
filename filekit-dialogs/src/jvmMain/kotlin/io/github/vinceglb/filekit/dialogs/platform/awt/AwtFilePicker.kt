package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.File
import java.io.FilenameFilter
import kotlin.coroutines.resume

internal class AwtFilePicker : PlatformFilePicker {
    override suspend fun openFilePicker(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? = callAwtPicker(
        title = title,
        isMultipleMode = false,
        fileExtensions = fileExtensions,
        initialDirectory = initialDirectory,
        parentWindow = dialogSettings.parentWindow
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = callAwtPicker(
        title = title,
        isMultipleMode = true,
        fileExtensions = fileExtensions,
        initialDirectory = initialDirectory,
        parentWindow = dialogSettings.parentWindow
    )

    override suspend fun openDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? {
        throw UnsupportedOperationException("Directory picker is not supported on Linux yet.")
    }

    private suspend fun callAwtPicker(
        title: String?,
        isMultipleMode: Boolean,
        initialDirectory: String?,
        fileExtensions: List<String>?,
        parentWindow: Window?
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val result = files?.toList()
                continuation.resume(result)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (parentWindow) {
            is Dialog -> object : FileDialog(parentWindow, title, LOAD) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(parentWindow as? Frame, title, LOAD) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set multiple mode
        dialog.isMultipleMode = isMultipleMode

        // MaxItems is not supported by FileDialog

        // Set mime types
        dialog.filenameFilter = FilenameFilter { _, name ->
            fileExtensions?.any { name.endsWith(it) } ?: true
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
