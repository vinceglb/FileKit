package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.ExperimentalFileKitApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@OptIn(ExperimentalFileKitApi::class)
@Composable
internal actual fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): FileSaverLauncher {
    val scope = rememberCoroutineScope()

    return remember(scope, dialogSettings, onError, onResult) {
        object : FileSaverLauncher {
            override val isSupported: Boolean = true
            override val supportsDirectorySelection: Boolean = false
            override val supportMessage: String =
                "Web opens the native save picker when available. Otherwise, writing the file downloads it. " +
                    "Save In is ignored on web."

            override fun launch(
                suggestedName: String,
                defaultExtension: String?,
                allowedExtensions: Set<String>?,
                directory: PlatformFile?,
            ) {
                // Run up to showSaveFilePicker() synchronously to preserve the click's user activation.
                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    try {
                        val file = FileKit.openFileSaver(
                            suggestedName = suggestedName,
                            defaultExtension = defaultExtension,
                            allowedExtensions = allowedExtensions,
                            directory = directory,
                            dialogSettings = dialogSettings,
                        )
                        if (file != null) {
                            if (file.extension.equals("pdf", ignoreCase = true)) {
                                file.write(createSamplePdf())
                            } else {
                                file.writeString("FileKit web file saver sample")
                            }
                        }
                        onResult(file)
                    } catch (failure: FileKitDialogException) {
                        onError(failure)
                    } catch (failure: CancellationException) {
                        throw failure
                    } catch (failure: Exception) {
                        onError(FileKitDialogException("Could not write the selected file", failure))
                    }
                }
            }
        }
    }
}

private fun createSamplePdf(): ByteArray {
    val text = "FileKit web file saver sample"
    val contentStream = "BT\n/F1 18 Tf\n72 720 Td\n($text) Tj\nET"
    val objects = listOf(
        "<< /Type /Catalog /Pages 2 0 R >>",
        "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
            "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        "<< /Length ${contentStream.encodeToByteArray().size} >>\nstream\n$contentStream\nendstream",
    )
    val pdf = StringBuilder("%PDF-1.4\n")
    val objectOffsets = mutableListOf<Int>()

    objects.forEachIndexed { index, body ->
        objectOffsets += pdf.toString().encodeToByteArray().size
        pdf.append("${index + 1} 0 obj\n$body\nendobj\n")
    }

    val crossReferenceOffset = pdf.toString().encodeToByteArray().size
    pdf.append("xref\n0 ${objects.size + 1}\n")
    pdf.append("0000000000 65535 f \n")
    objectOffsets.forEach { offset ->
        pdf.append("${offset.toString().padStart(10, '0')} 00000 n \n")
    }
    pdf.append(
        "trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\n" +
            "startxref\n$crossReferenceOffset\n%%EOF\n",
    )
    return pdf.toString().encodeToByteArray()
}
