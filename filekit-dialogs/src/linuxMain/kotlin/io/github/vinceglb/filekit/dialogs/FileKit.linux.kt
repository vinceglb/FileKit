@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.platform.linux.LinuxXdgPortalException
import io.github.vinceglb.filekit.dialogs.platform.linux.PortalRequestMethod
import io.github.vinceglb.filekit.dialogs.platform.linux.PortalVariant
import io.github.vinceglb.filekit.dialogs.platform.linux.buildPortalFileFilters
import io.github.vinceglb.filekit.dialogs.platform.linux.runXdgPortalRequest
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import platform.posix.EXIT_FAILURE
import platform.posix._exit
import platform.posix.execvp
import platform.posix.fork

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
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
    return withContext(Dispatchers.IO) {
        runLinuxNativePickerOperation {
            openPortalDialog(
                directory = directory,
                fileExtensions = extensions,
                title = dialogSettings.title,
                multiple = mode is PickerMode.Multiple,
                openDirectory = false,
            )
        }
    }.toPickerStateFlow()
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
): PlatformFile? = withContext(Dispatchers.IO) {
    try {
        openPortalDialog(
            directory = directory,
            fileExtensions = null,
            title = dialogSettings.title,
            multiple = false,
            openDirectory = true,
        )?.firstOrNull()
    } catch (failure: LinuxXdgPortalException) {
        throw FileKitDialogException(
            message = LINUX_DIRECTORY_PICKER_FAILURE_MESSAGE,
            cause = failure,
        )
    }
}

internal actual suspend fun FileKit.platformOpenFileSaver(
    suggestedName: String,
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? = withContext(Dispatchers.IO) {
    val extension = normalizeFileSaverExtension(defaultExtension)
    val filters = normalizeFileSaverExtensions(allowedExtensions)
    try {
        val options = mutableMapOf<String, PortalVariant>(
            "current_name" to PortalVariant.Str(buildFileSaverSuggestedName(suggestedName, extension)),
        )
        filters?.let { options["filters"] = PortalVariant.Filters(buildPortalFileFilters(it)) }
        directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

        runXdgPortalRequest(
            method = PortalRequestMethod.SaveFile,
            parentWindow = "",
            title = dialogSettings.title.orEmpty(),
            options = options,
        )?.firstOrNull()
            ?.let { PlatformFile(it) }
    } catch (failure: LinuxXdgPortalException) {
        throw FileKitDialogException(
            message = LINUX_FILE_SAVER_FAILURE_MESSAGE,
            cause = failure,
        )
    }
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
    openWithXdgOpen(file.path)
}

private fun openPortalDialog(
    directory: PlatformFile?,
    fileExtensions: Set<String>?,
    title: String?,
    multiple: Boolean,
    openDirectory: Boolean,
): List<PlatformFile>? {
    val options = mutableMapOf<String, PortalVariant>(
        "multiple" to PortalVariant.Bool(multiple),
        "directory" to PortalVariant.Bool(openDirectory),
    )
    fileExtensions?.let { options["filters"] = PortalVariant.Filters(buildPortalFileFilters(it)) }
    directory?.let { options["current_folder"] = createCurrentFolderOption(it) }

    return runXdgPortalRequest(
        method = PortalRequestMethod.OpenFile,
        parentWindow = "",
        title = title.orEmpty(),
        options = options,
    )?.map { path -> PlatformFile(path) }
}

/**
 * Builds the `current_folder` portal option: a null-terminated bytestring holding the raw
 * filesystem path of the folder, as expected by the portal FileChooser.
 */
private fun createCurrentFolderOption(folder: PlatformFile): PortalVariant.Bytes {
    val path = folder.path
    val bytes = ByteArray(path.length + 1)
    path.encodeToByteArray().copyInto(bytes)
    return PortalVariant.Bytes(bytes)
}

internal fun <T> runLinuxNativePickerOperation(operation: () -> T): T = try {
    operation()
} catch (failure: LinuxXdgPortalException) {
    throw FileKitPickerException(
        message = LINUX_FILE_PICKER_FAILURE_MESSAGE,
        cause = failure,
    )
}

private fun openWithXdgOpen(path: String) {
    val spawnResult = memScoped {
        val executable = "xdg-open".cstr
        val pathArgument = path.cstr
        val arguments = allocArray<CPointerVar<ByteVar>>(3)
        arguments[0] = executable.ptr
        arguments[1] = pathArgument.ptr
        arguments[2] = null

        val pid = fork()
        if (pid == 0) {
            execvp("xdg-open", arguments)
            _exit(EXIT_FAILURE)
        }
        pid
    }
    if (spawnResult < 0) {
        throw FileKitException("Could not open the file with the default application.")
    }
}
