package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual suspend fun <Out> FileKit.openFilePicker(
    type: PickerType,
    mode: PickerMode<Out>,
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): Out? = withContext(Dispatchers.IO) {
    // Filter by extension
    val extensions = when (type) {
        PickerType.Image -> imageExtensions
        PickerType.Video -> videoExtensions
        PickerType.ImageAndVideo -> imageExtensions + videoExtensions
        is PickerType.File -> type.extensions
    }

    // Open native file picker
    val result = when (mode) {
        is PickerMode.Single -> PlatformFilePicker.current.pickFile(
            title = title,
            initialDirectory = initialDirectory,
            fileExtensions = extensions,
            platformSettings = platformSettings,
        )?.let { listOf(PlatformFile(it)) }

        is PickerMode.Multiple -> PlatformFilePicker.current.pickFiles(
            title = title,
            initialDirectory = initialDirectory,
            fileExtensions = extensions,
            platformSettings = platformSettings,
        )?.map { PlatformFile(it) }
    }

    // Return result
    mode.parseResult(result)
}

public actual suspend fun FileKit.pickDirectory(
    title: String?,
    initialDirectory: String?,
    platformSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    // Open native file picker
    val file = PlatformFilePicker.current.pickDirectory(
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
    val result = PlatformFilePicker.current.saveFile(
        baseName = baseName,
        extension = extension,
        initialDirectory = initialDirectory,
        platformSettings = platformSettings,
    )
    result?.let { PlatformFile(result) }
}
