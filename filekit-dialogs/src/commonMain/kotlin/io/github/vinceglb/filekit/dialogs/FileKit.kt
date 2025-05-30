package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

public expect suspend fun <Out> FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<Out>,
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): Out?

public suspend fun FileKit.openFilePicker(
    type: FileKitType = FileKitType.File(),
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? {
    return openFilePicker(
        type = type,
        mode = FileKitMode.Single,
        title = title,
        directory = directory,
        dialogSettings = dialogSettings,
    )
}

public expect suspend fun <Out> FileKit.openFilePickerWithProgressUpdate(
    type: FileKitType = FileKitType.File(),
    mode: FileKitMode<Out>,
    title: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): Flow<FilePickerResult>

public sealed class FilePickerResult {
    public data object FilePickerAborted : FilePickerResult()

    public data class FileImportStarting(val count: Int) : FilePickerResult()

    /**
     * @param completed: The files that have been imported and can be used by the app
     * @param totalCount: The total count of files being imported, remainingCount = totalCount - completed.size
     */
    public data class FileImportProgressUpdate(
        val completed: List<PlatformFile>,
        val totalCount: Int,
    ) : FilePickerResult()

    public data class FilePickerCompleted(val pickedFiles: List<PlatformFile>) : FilePickerResult()
}
