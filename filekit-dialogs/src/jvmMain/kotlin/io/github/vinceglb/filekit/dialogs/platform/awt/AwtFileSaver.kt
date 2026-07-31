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
        defaultExtension: String?,
        allowedExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings?,
    ): File? = suspendCancellableCoroutine { continuation ->
        fun handleResult(value: Boolean, files: Array<File>?) {
            if (value && continuation.isActive) {
                val file = files?.firstOrNull()
                continuation.resume(file)
            }
        }

        // Handle parentWindow: Dialog, Frame, or null
        val dialog = runAwtDialogOperation("Failed to initialize the AWT file saver") {
            when (dialogSettings?.parentWindow) {
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
        }

        // Dispose the dialog when the continuation is cancelled.
        continuation.invokeOnCancellation { dialog.dispose() }

        runAwtDialogOperation("Failed to present the AWT file saver") {
            // Set initial directory
            directory?.let { dialog.directory = directory.path }

            val filterExtensions = allowedExtensions ?: defaultExtension?.let { setOf(it) }
            filterExtensions?.let { extensions ->
                dialog.filenameFilter = java.io.FilenameFilter { _, name ->
                    extensions.any { extension -> name.endsWith(".$extension", ignoreCase = true) }
                }
            }

            // Set file name
            dialog.file = when {
                defaultExtension != null -> "$suggestedName.$defaultExtension"
                else -> suggestedName
            }

            // Show the dialog
            dialog.isVisible = true
        }
    }
}
