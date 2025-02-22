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
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
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
            initialDirectory = initialDirectory,
            fileExtensions = extensions,
            platformSettings = platformSettings,
        )?.let { listOf(PlatformFile(it)) }

        is FileKitMode.Multiple -> PlatformFilePicker.current.openFilesPicker(
            title = title,
            initialDirectory = initialDirectory,
            fileExtensions = extensions,
            platformSettings = platformSettings,
        )?.map { PlatformFile(it) }
    }

    // Return result
    mode.parseResult(result)
}

public actual suspend fun FileKit.openDirectoryPicker(
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    // Open native file picker
    val file = PlatformFilePicker.current.openDirectoryPicker(
        title = title,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )

    // Return result
    file?.let { PlatformFile(it) }
}

public actual suspend fun FileKit.openFileSaver(
    baseName: String,
    extension: String,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    val result = PlatformFilePicker.current.openFileSaver(
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
    result?.let { PlatformFile(result) }
}
