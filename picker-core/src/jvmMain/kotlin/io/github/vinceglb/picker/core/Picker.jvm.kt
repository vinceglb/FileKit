package io.github.vinceglb.picker.core

import io.github.vinceglb.picker.core.platform.PlatformFilePicker
import io.github.vinceglb.picker.core.platform.awt.AwtFileSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public actual object Picker {
    public actual suspend fun <Out> pick(
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?
    ): Out? = withContext(Dispatchers.IO) {
        val picker = PlatformFilePicker.current

        // Open native file picker
        val selection = when (mode) {
            is PickerSelectionMode.SingleFile -> picker.pickFile(
                title = title,
                initialDirectory = initialDirectory,
                fileExtensions = mode.extensions
            )?.let { listOf(it) }

            is PickerSelectionMode.MultipleFiles -> picker.pickFiles(
                title = title,
                initialDirectory = initialDirectory,
                fileExtensions = mode.extensions
            )

            is PickerSelectionMode.Directory -> picker.pickDirectory(
                title = title,
                initialDirectory = initialDirectory
            )?.let { listOf(it) }

            else -> throw IllegalArgumentException("Unsupported mode: $mode")
        }.let { PickerSelectionMode.SelectionResult(it) }

        // Return result
        return@withContext mode.result(selection)
    }

    public actual suspend fun save(
        bytes: ByteArray,
        fileName: String,
        initialDirectory: String?
    ): PlatformFile? = withContext(Dispatchers.IO) {
        AwtFileSaver.saveFile(
            bytes = bytes,
            fileName = fileName,
            initialDirectory = initialDirectory
        )
    }
}
