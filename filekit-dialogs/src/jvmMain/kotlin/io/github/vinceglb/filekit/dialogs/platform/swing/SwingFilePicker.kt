package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
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

    override suspend fun openFilePicker(
        initialDirectory: String?,
        fileExtensions: Set<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = false,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        dialogSettings = dialogSettings,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        initialDirectory: String?,
        fileExtensions: Set<String>?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = true,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        dialogSettings = dialogSettings,
    )

    override suspend fun openDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        dialogSettings: FileKitDialogSettings,
    ): File? =
        callSwingFilePicker(
            title = title,
            mode = JFileChooser.DIRECTORIES_ONLY,
            isMultiSelectionEnabled = false,
            initialDirectory = initialDirectory,
            fileExtensions = null,
            dialogSettings = dialogSettings,
        )?.firstOrNull()

    private suspend fun callSwingFilePicker(
        title: String?,
        mode: Int,
        isMultiSelectionEnabled: Boolean,
        initialDirectory: String?,
        fileExtensions: Set<String>?,
        dialogSettings: FileKitDialogSettings,
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

        val returnValue = jFileChooser.showOpenDialog(dialogSettings.parentWindow)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            continuation.resume(jFileChooser.selectedFiles.toList())
        }

        continuation.invokeOnCancellation { jFileChooser.cancelSelection() }
    }
}