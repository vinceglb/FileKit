package io.github.vinceglb.filekit.core

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.awt.AwtFileSaver
import io.github.vinceglb.filekit.core.platform.util.Platform
import io.github.vinceglb.filekit.core.platform.util.PlatformUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual object Picker {
    public actual suspend fun <Out> pickFile(
        type: PickerSelectionType,
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): Out? = withContext(Dispatchers.IO) {
        // Filter by extension
        val extensions = when (type) {
            PickerSelectionType.Image -> imageExtensions
            PickerSelectionType.Video -> videoExtensions
            PickerSelectionType.ImageAndVideo -> imageExtensions + videoExtensions
            is PickerSelectionType.File -> type.extensions
        }

        // Open native file picker
        val result = when (mode) {
            PickerSelectionMode.Single -> PlatformFilePicker.current.pickFile(
                title = title,
                initialDirectory = initialDirectory,
                fileExtensions = extensions,
                parentWindow = platformSettings?.parentWindow,
            )?.let { listOf(PlatformFile(it)) }

            PickerSelectionMode.Multiple -> PlatformFilePicker.current.pickFiles(
                title = title,
                initialDirectory = initialDirectory,
                fileExtensions = extensions,
                parentWindow = platformSettings?.parentWindow,
            )?.map { PlatformFile(it) }
        }

        // Return result
        mode.parseResult(result)
    }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): PlatformDirectory? = withContext(Dispatchers.IO) {
        // Open native file picker
        val file = PlatformFilePicker.current.pickDirectory(
            title = title,
            initialDirectory = initialDirectory,
            parentWindow = platformSettings?.parentWindow,
        )

        // Return result
        file?.let { PlatformDirectory(it) }
    }

    public actual fun isDirectoryPickerSupported(): Boolean = when (PlatformUtil.current) {
        Platform.MacOS -> true
        Platform.Windows -> true
        Platform.Linux -> false
    }

    public actual suspend fun saveFile(
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): PlatformFile? = withContext(Dispatchers.IO) {
        AwtFileSaver.saveFile(
            bytes = bytes,
            baseName = baseName,
            extension = extension,
            initialDirectory = initialDirectory,
            parentWindow = platformSettings?.parentWindow,
        )
    }
}
