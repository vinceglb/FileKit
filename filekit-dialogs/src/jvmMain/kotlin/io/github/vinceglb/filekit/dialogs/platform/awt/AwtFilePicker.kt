package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
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
import kotlin.coroutines.resumeWithException

internal class AwtFilePicker : PlatformFilePicker {
    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = callAwtPicker(
        title = dialogSettings.title,
        isMultipleMode = false,
        fileExtensions = fileExtensions,
        directory = directory,
        parentWindow = dialogSettings.parentWindow,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = callAwtPicker(
        title = dialogSettings.title,
        isMultipleMode = true,
        fileExtensions = fileExtensions,
        directory = directory,
        parentWindow = dialogSettings.parentWindow,
    )

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = throw JvmDialogOperationException("Directory picker is not supported on Linux yet.")

    private suspend fun callAwtPicker(
        title: String?,
        isMultipleMode: Boolean,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        parentWindow: Window?,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        // Handle parentWindow: Dialog, Frame, or null
        val dialog = runAwtDialogOperation("Failed to initialize the AWT file picker") {
            when (parentWindow) {
                is Dialog -> FileDialog(parentWindow, title, LOAD)
                else -> FileDialog(parentWindow as? Frame, title, LOAD)
            }
        }

        continuation.invokeOnCancellation { dialog.dispose() }

        EventQueue.invokeLater {
            dispatchAwtDialogOperation(
                isActive = { continuation.isActive },
                operation = {
                    runAwtDialogOperation("Failed to present the AWT file picker") {
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
                        files ?: dialog.file?.let { arrayOf(File(it)) }
                    }?.toList()
                },
                onResult = continuation::resume,
                onFailure = continuation::resumeWithException,
            )
        }
    }
}
