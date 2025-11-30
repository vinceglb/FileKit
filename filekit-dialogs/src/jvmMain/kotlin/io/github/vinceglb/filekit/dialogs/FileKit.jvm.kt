package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.awt.Desktop

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> {
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
                    title = title,
                    directory = directory,
                    fileExtensions = extensions,
                    dialogSettings = dialogSettings,
                )?.let { listOf(PlatformFile(it)) }
        }

        is PickerMode.Multiple -> {
            PlatformFilePicker.current
                .openFilesPicker(
                    title = title,
                    directory = directory,
                    fileExtensions = extensions,
                    dialogSettings = dialogSettings,
                )?.map { PlatformFile(it) }
        }
    }

    return files.toPickerStateFlow()
}

/**
 * Opens a directory picker dialog.
 *
 * @param title The title of the dialog. Supported on desktop platforms.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The picked directory as a [PlatformFile], or null if cancelled.
 */
public actual suspend fun FileKit.openDirectoryPicker(
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    // Open native file picker
    val file = PlatformFilePicker.current.openDirectoryPicker(
        title = title,
        directory = directory,
        dialogSettings = dialogSettings,
    )

    // Return result
    file?.let { PlatformFile(it) }
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
public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    val result = PlatformFilePicker.current.openFileSaver(
        suggestedName = suggestedName,
        extension = extension,
        directory = directory,
        dialogSettings = dialogSettings,
    )
    result?.let { PlatformFile(result) }
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
