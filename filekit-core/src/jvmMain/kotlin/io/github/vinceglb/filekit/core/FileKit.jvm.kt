package io.github.vinceglb.filekit.core

import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.util.Platform
import io.github.vinceglb.filekit.core.platform.util.PlatformUtil
import io.github.vinceglb.filekit.core.platform.xdg.XdgFilePickerPortal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual object FileKit {
    public actual suspend fun <Out> pickFile(
        type: PickerType,
        mode: PickerMode<Out>,
        title: String?,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
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
                parentWindow = platformSettings?.parentWindow,
            )?.let { listOf(PlatformFile(it)) }

            is PickerMode.Multiple -> PlatformFilePicker.current.pickFiles(
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
        platformSettings: FileKitPlatformSettings?,
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
        Platform.Linux -> PlatformFilePicker.current is XdgFilePickerPortal
    }

    public actual suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): PlatformFile? = withContext(Dispatchers.IO) {
        val result = PlatformFilePicker.current.saveFile(
            bytes = bytes,
            baseName = baseName,
            extension = extension,
            initialDirectory = initialDirectory,
            parentWindow = platformSettings?.parentWindow,
        )
        result?.let { PlatformFile(result) }
    }

    public actual suspend fun isSaveFileWithoutBytesSupported(): Boolean = true
}
