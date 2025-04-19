package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Dialog
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.coroutines.resume

internal object AwtFileSaver {
    suspend fun saveFile(
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings?,
    ): File? = suspendCancellableCoroutine { continuation ->
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value) {
                val file = files?.firstOrNull()
                continuation.resume(file)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = when (dialogSettings?.parentWindow) {
            is Dialog -> object : FileDialog(dialogSettings.parentWindow, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }

            else -> object : FileDialog(dialogSettings?.parentWindow as? Frame, "Save dialog", SAVE) {
                override fun setVisible(value: Boolean) {
                    super.setVisible(value)
                    handleResult(value, files)
                }
            }
        }

        // Set initial directory
        directory?.let { dialog.directory = directory.path }

        // Set file name
        dialog.file = when {
            extension != null -> "$suggestedName.$extension"
            else -> suggestedName
        }

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
