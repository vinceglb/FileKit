package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import io.github.vinceglb.filekit.dialogs.platform.awt.runAwtDialogOperation
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume

internal class SwingFilePicker : PlatformFilePicker {
    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (_: ReflectiveOperationException) {
            println("Failed to set native UI for JFileChooser")
        } catch (_: UnsupportedLookAndFeelException) {
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
    ): File? =
        callSwingFilePicker(
            mode = JFileChooser.DIRECTORIES_ONLY,
            isMultiSelectionEnabled = false,
            directory = directory,
            fileExtensions = null,
            dialogSettings = dialogSettings,
        )?.firstOrNull()

    private suspend fun callSwingFilePicker(
        mode: Int,
        isMultiSelectionEnabled: Boolean,
        directory: PlatformFile?,
        fileExtensions: Set<String>?,
        dialogSettings: FileKitDialogSettings,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        val jFileChooser = runAwtDialogOperation("Failed to initialize the Swing file picker") {
            JFileChooser(directory?.path).apply {
                fileSelectionMode = mode
                this.isMultiSelectionEnabled = isMultiSelectionEnabled

                if (fileExtensions != null) {
                    val filter = FileNameExtensionFilter(null, *fileExtensions.toTypedArray())
                    addChoosableFileFilter(filter)
                }

                if (dialogSettings.title != null) {
                    dialogTitle = dialogSettings.title
                }
            }
        }

        continuation.invokeOnCancellation { jFileChooser.cancelSelection() }

        val returnValue = runAwtDialogOperation("Failed to present the Swing file picker") {
            jFileChooser.showOpenDialog(dialogSettings.parentWindow)
        }
        continuation.resume(
            resolveSwingDialogResult(
                returnValue = returnValue,
                selectedFiles = jFileChooser.selectedFiles,
                selectedFile = jFileChooser.selectedFile,
            ),
        )
    }
}

internal fun resolveSwingDialogResult(
    returnValue: Int,
    selectedFiles: Array<File>,
    selectedFile: File?,
): List<File>? = when (returnValue) {
    JFileChooser.CANCEL_OPTION -> {
        null
    }

    JFileChooser.APPROVE_OPTION -> {
        selectedFiles
            .toList()
            .takeIf { it.isNotEmpty() }
            ?: selectedFile?.let(::listOf)
            ?: throw JvmDialogOperationException("The Swing file picker returned no selection")
    }

    else -> {
        throw JvmDialogOperationException("The Swing file picker failed with result code $returnValue")
    }
}
