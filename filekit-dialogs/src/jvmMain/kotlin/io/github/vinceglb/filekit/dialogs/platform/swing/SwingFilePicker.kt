package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.requireAwtWindowOrNull
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.HeadlessException
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume

internal class SwingFilePicker : PlatformFilePicker {
    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: Throwable) {
            println("Failed to set native UI for JFileChooser")
        }
    }

    override suspend fun openFilePicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = callSwingFilePicker(
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = false,
        directory = directory,
        fileExtensions = fileExtensions,
        dialogSettings = dialogSettings,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        fileExtensions: Set<String>?,
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = callSwingFilePicker(
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = true,
        directory = directory,
        fileExtensions = fileExtensions,
        dialogSettings = dialogSettings,
    )

    override suspend fun openDirectoryPicker(
        directory: PlatformFile?,
        dialogSettings: FileKitDialogSettings,
    ): File? = runSwingDirectoryPicker {
        callSwingFilePicker(
            mode = JFileChooser.DIRECTORIES_ONLY,
            isMultiSelectionEnabled = false,
            directory = directory,
            fileExtensions = null,
            dialogSettings = dialogSettings,
        )?.firstOrNull()
    }

    private suspend fun callSwingFilePicker(
        mode: Int,
        isMultiSelectionEnabled: Boolean,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        val jFileChooser = JFileChooser(directory?.path)
        jFileChooser.fileSelectionMode = mode
        jFileChooser.isMultiSelectionEnabled = isMultiSelectionEnabled

        if (fileExtensions != null) {
            val filter = FileNameExtensionFilter(null, *fileExtensions.toTypedArray())
            jFileChooser.addChoosableFileFilter(filter)
        }

        if (dialogSettings.title != null) {
            jFileChooser.dialogTitle = dialogSettings.title
        }

        val parentWindow = dialogSettings.parent.requireAwtWindowOrNull("Swing dialogs")
        val returnValue = jFileChooser.showOpenDialog(parentWindow)
        continuation.resume(
            resolveSwingPickerResult(
                returnValue = returnValue,
                selectedFiles = jFileChooser.selectedFiles,
                selectedFile = jFileChooser.selectedFile,
            ),
        )

        continuation.invokeOnCancellation { jFileChooser.cancelSelection() }
    }
}

internal suspend fun <T> runSwingDirectoryPicker(
    operation: suspend () -> T,
): T = try {
    operation()
} catch (failure: HeadlessException) {
    throw FileKitDialogException(
        message = "The Swing directory picker is unavailable in a headless environment.",
        cause = failure,
    )
}

internal fun resolveSwingPickerResult(
    returnValue: Int,
    selectedFiles: Array<File>,
    selectedFile: File?,
): List<File>? = if (returnValue == JFileChooser.APPROVE_OPTION) {
    selectedFiles.toList().takeIf { it.isNotEmpty() } ?: selectedFile?.let(::listOf)
} else {
    null
}
