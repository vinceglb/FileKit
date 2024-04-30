package io.github.vinceglb.picker.core.platform.awt

import io.github.vinceglb.picker.core.PlatformFile
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.FileDialog
import java.awt.Frame
import kotlin.coroutines.resume

internal object AwtFileSaver {
    suspend fun saveFile(
        bytes: ByteArray,
        fileName: String,
        initialDirectory: String?,
    ): PlatformFile? = suspendCancellableCoroutine { continuation ->
        val parent: Frame? = null
        val dialog = object : FileDialog(parent, "Save dialog", SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)

                val file = files?.firstOrNull()?.let {
                    it.writeBytes(bytes)
                    PlatformFile(it)
                }

                continuation.resume(file)
            }
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Set file name
        dialog.file = fileName

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
