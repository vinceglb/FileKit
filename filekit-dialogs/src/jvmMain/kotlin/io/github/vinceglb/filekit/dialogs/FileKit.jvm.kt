package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType,
    mode: FileKitMode<Out>,
    title: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Out? = withContext(Dispatchers.IO) {
    // Filter by extension
    val extensions = when (type) {
        FileKitType.Image -> imageExtensions
        FileKitType.Video -> videoExtensions
        FileKitType.ImageAndVideo -> imageExtensions + videoExtensions
        is FileKitType.File -> type.extensions
    }

    // Open native file picker
    val result = when (mode) {
        is FileKitMode.Single -> PlatformFilePicker.current.openFilePicker(
            title = title,
            directory = directory,
            fileExtensions = extensions,
            dialogSettings = dialogSettings,
        )?.let { listOf(PlatformFile(it)) }

        is FileKitMode.Multiple -> PlatformFilePicker.current.openFilesPicker(
            title = title,
            directory = directory,
            fileExtensions = extensions,
            dialogSettings = dialogSettings,
        )?.map { PlatformFile(it) }
    }

    // Return result
    mode.parseResult(result)
}

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

public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String,
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
