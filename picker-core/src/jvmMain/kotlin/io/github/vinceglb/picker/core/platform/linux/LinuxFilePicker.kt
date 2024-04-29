package io.github.vinceglb.picker.core.platform.linux

import io.github.vinceglb.picker.core.PickerSelectionMode
import io.github.vinceglb.picker.core.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import kotlin.coroutines.resume

internal class LinuxFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?
    ): File? = callAwtPicker(
        mode = PickerSelectionMode.SingleFile(fileExtensions),
        title = title,
        initialDirectory = initialDirectory
    )?.file

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?
    ): List<File>? = callAwtPicker(
        mode = PickerSelectionMode.MultipleFiles(fileExtensions),
        title = title,
        initialDirectory = initialDirectory
    )?.map { it.file }

    override fun pickDirectory(initialDirectory: String?, title: String?): File? {
        throw UnsupportedOperationException("Directory picker is not supported on Linux yet.")
    }

    private suspend fun <T> callAwtPicker(
        mode: PickerSelectionMode<T>,
        title: String?,
        initialDirectory: String?,
    ): T? = suspendCancellableCoroutine { continuation ->
        val parent: Frame? = null
        val dialog = object : FileDialog(parent, title, LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                val files: List<File>? = files?.toList()
                val selection = PickerSelectionMode.SelectionResult(files)
                continuation.resume(mode.result(selection))
            }
        }

        // Set multiple mode
        dialog.isMultipleMode = mode is PickerSelectionMode.MultipleFiles

        // Set mime types
        dialog.filenameFilter = FilenameFilter { dir, name ->
            when (mode) {
                is PickerSelectionMode.SingleFile -> mode.extensions?.any { name.endsWith(it) }
                    ?: true

                is PickerSelectionMode.MultipleFiles -> mode.extensions?.any { name.endsWith(it) }
                    ?: true

                else -> throw IllegalArgumentException("Unsupported mode: $mode")
            }
        }

        // Set initial directory
        dialog.directory = initialDirectory

        // Show the dialog
        dialog.isVisible = true

        // Dispose the dialog when the continuation is cancelled
        continuation.invokeOnCancellation { dialog.dispose() }
    }
}
