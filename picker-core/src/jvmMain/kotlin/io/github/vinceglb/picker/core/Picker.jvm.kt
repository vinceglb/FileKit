package io.github.vinceglb.picker.core

import io.github.vinceglb.picker.core.platform.PlatformFilePicker
import io.github.vinceglb.picker.core.platform.awt.AwtFileSaver
import io.github.vinceglb.picker.core.platform.util.Platform
import io.github.vinceglb.picker.core.platform.util.PlatformUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual object Picker {
    public actual suspend fun <Out> pickFile(
        type: PickerSelectionType,
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?
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
                fileExtensions = extensions
            )?.let { listOf(PlatformFile(it)) }

            PickerSelectionMode.Multiple -> PlatformFilePicker.current.pickFiles(
                title = title,
                initialDirectory = initialDirectory,
                fileExtensions = extensions
            )?.map { PlatformFile(it) }
        }

        // Return result
        mode.parseResult(result)
    }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?
    ): PlatformDirectory? = withContext(Dispatchers.IO) {
        // Open native file picker
        val file = PlatformFilePicker.current.pickDirectory(
            title = title,
            initialDirectory = initialDirectory
        )

        // Return result
        file?.let { PlatformDirectory(it) }
    }

    public actual fun isPickDirectorySupported(): Boolean = when (PlatformUtil.current) {
        Platform.MacOS -> true
        Platform.Windows -> true
        Platform.Linux -> false
    }

    public actual suspend fun saveFile(
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ): PlatformFile? = withContext(Dispatchers.IO) {
        AwtFileSaver.saveFile(
            bytes = bytes,
            baseName = baseName,
            extension = extension,
            initialDirectory = initialDirectory
        )
    }
}
