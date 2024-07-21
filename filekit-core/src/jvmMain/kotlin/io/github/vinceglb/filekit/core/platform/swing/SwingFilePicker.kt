package io.github.vinceglb.filekit.core.platform.swing

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Window
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume

internal class SwingFilePicker : PlatformFilePicker {

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            println("Failed to set native UI for JFileChooser")
        }
    }

    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?
    ): File? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = false,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        parentWindow = parentWindow,
    )?.firstOrNull()

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        parentWindow: Window?
    ): List<File>? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = true,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        parentWindow = parentWindow,
    )

    override suspend fun pickDirectory(initialDirectory: String?, title: String?, parentWindow: Window?): File? =
        callSwingFilePicker(
            title = title,
            mode = JFileChooser.DIRECTORIES_ONLY,
            isMultiSelectionEnabled = false,
            initialDirectory = initialDirectory,
            fileExtensions = null,
            parentWindow = parentWindow,
        )?.firstOrNull()

    private suspend fun callSwingFilePicker(
        title: String?,
        mode: Int,
        isMultiSelectionEnabled: Boolean,
        initialDirectory: String?,
        fileExtensions: List<String>?,
        parentWindow: Window?,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        val jFileChooser = JFileChooser(initialDirectory)
        jFileChooser.fileSelectionMode = mode
        jFileChooser.isMultiSelectionEnabled = isMultiSelectionEnabled

        if(fileExtensions != null) {
            val filter = FileNameExtensionFilter(null, *fileExtensions.toTypedArray())
            jFileChooser.addChoosableFileFilter(filter)
        }

        if (title != null) {
            jFileChooser.dialogTitle = title
        }

        val returnValue = jFileChooser.showOpenDialog(parentWindow)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            continuation.resume(jFileChooser.selectedFiles.toList())
        }

        continuation.invokeOnCancellation { jFileChooser.cancelSelection() }
    }
}