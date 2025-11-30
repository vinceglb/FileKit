package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Dialog
import java.awt.EventQueue
import java.awt.FileDialog
import java.awt.FileDialog.LOAD
import java.awt.Frame
import java.awt.Window
import java.io.File
import java.io.FilenameFilter
import kotlin.coroutines.resume

internal class AwtFilePicker : PlatformFilePicker {
    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = callAwtPicker(
        title = title,
        isMultipleMode = false,
        fileExtensions = fileExtensions,
        directory = directory,
        parentWindow = dialogSettings.parentWindow,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = callAwtPicker(
        title = title,
        isMultipleMode = true,
        fileExtensions = fileExtensions,
        directory = directory,
        parentWindow = dialogSettings.parentWindow,
    )

    override suspend fun openDirectoryPicker(
        title: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = throw UnsupportedOperationException("Directory picker is not supported on Linux yet.")

    private suspend fun callAwtPicker(
        title: String?,
        isMultipleMode: Boolean,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        parentWindow: Window?,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (parentWindow) {
            is Dialog -> FileDialog(parentWindow, title, LOAD)
            else -> FileDialog(parentWindow as? Frame, title, LOAD)
        }

        EventQueue.invokeLater {
            // Set multiple mode
            dialog.isMultipleMode = isMultipleMode

            // Set mime types
            dialog.filenameFilter = FilenameFilter { _, name ->
                fileExtensions?.any { name.endsWith(suffix = it) } ?: true
            }

            // Set initial directory
            directory?.let { dialog.directory = directory.path }

            // Show the dialog
            dialog.isVisible = true

            val files = dialog.files.takeIf { it.isNotEmpty() }
            val result = files ?: dialog.file?.let { arrayOf(File(it)) }

            continuation.resume(value = result?.toList())
        }

        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
