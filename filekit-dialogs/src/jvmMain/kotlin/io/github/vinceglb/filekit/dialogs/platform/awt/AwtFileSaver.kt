package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.coroutines.resume

internal object AwtFileSaver {
    suspend fun saveFile(
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitDialogSettings?,
    ): File? = suspendCancellableCoroutine { continuation ->
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val file = files?.firstOrNull()?.let { file ->
                    // Write bytes to file, or create a new file
                    // bytes?.let { file.writeBytes(bytes) } ?: file.createNewFile()
                    file
                }
                continuation.resume(file)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (platformSettings?.parentWindow) {
            is Dialog -> object : FileDialog(platformSettings.parentWindow, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(platformSettings?.parentWindow as? Frame, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Set file name
        dialog.file = "$baseName.$extension"

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
