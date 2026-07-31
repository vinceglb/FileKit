package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.awt.Desktop

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> = runJvmDialogOperation(
    failure = { FileKitPickerException("Failed to open the file picker.", it) },
) {
    withContext(Dispatchers.IO) {
        // Filter by extension
        val extensions = when (type) {
            FileKitType.Image -> imageExtensions
            FileKitType.Video -> videoExtensions
            FileKitType.ImageAndVideo -> imageExtensions + videoExtensions
            is FileKitType.File -> type.extensions
        }

        val files = when (mode) {
            PickerMode.Single -> {
                PlatformFilePicker.current
                    .openFilePicker(
                        directory = directory,
                        fileExtensions = extensions,
                        dialogSettings = dialogSettings,
                    )?.let { listOf(PlatformFile(it)) }
            }

            is PickerMode.Multiple -> {
                PlatformFilePicker.current
                    .openFilesPicker(
                        directory = directory,
                        fileExtensions = extensions,
                        dialogSettings = dialogSettings,
                    )?.map { PlatformFile(it) }
            }
        }

        files.toPickerStateFlow()
    }
}

/**
 * Opens a directory picker dialog.
 *
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked directory as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = runJvmDialogOperation(
    failure = { FileKitDialogException("Failed to open the directory picker.", it) },
) {
    withContext(Dispatchers.IO) {
        // Open native file picker
        val file = PlatformFilePicker.current.openDirectoryPicker(
            directory = directory,
            dialogSettings = dialogSettings,
        )

        // Return result
        file?.let { PlatformFile(it) }
    }
}

/**
 * Opens a file saver dialog.
 *
 * @param suggestedName The suggested name for the file.
 * @param extension The file extension (optional).
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if cancelled.
 */
internal actual suspend fun FileKit.platformOpenFileSaver(
    suggestedName: String,
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = runJvmDialogOperation(
    failure = { FileKitDialogException("Failed to open the file saver.", it) },
) {
    withContext(Dispatchers.IO) {
        val normalizedDefaultExtension = normalizeFileSaverExtension(defaultExtension)
        val normalizedAllowedExtensions = normalizeFileSaverExtensions(allowedExtensions)
        val result = PlatformFilePicker.current.openFileSaver(
            suggestedName = suggestedName,
            defaultExtension = normalizedDefaultExtension,
            allowedExtensions = normalizedAllowedExtensions,
            directory = directory,
            dialogSettings = dialogSettings,
        )
        result?.let { PlatformFile(result) }
    }
}

internal suspend fun <Result> runJvmDialogOperation(
    failure: (Throwable) -> FileKitDialogException,
    block: suspend () -> Result,
): Result = try {
    block()
} catch (cause: CancellationException) {
    throw cause
} catch (cause: FileKitDialogException) {
    throw cause
} catch (cause: JvmDialogOperationException) {
    throw failure(cause.cause ?: cause)
}

/**
 * Opens a file with the default application associated with its file type.
 *
 * @param file The file to open.
 * @param openFileSettings Platform-specific settings for opening the file.
 */
public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings,
) {
    Desktop.getDesktop()?.open(file.file)
}
