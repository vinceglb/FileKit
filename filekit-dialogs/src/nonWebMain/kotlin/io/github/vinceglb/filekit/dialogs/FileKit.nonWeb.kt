package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

/**
 * Opens a file saver dialog.
 *
 * [defaultExtension] controls the suggested/default extension for the generated
 * file name. [allowedExtensions] controls native save dialog filters where the
 * platform supports them; apps that require a specific output format should
 * still validate the returned file extension.
 *
 * @param suggestedName The suggested name for the file.
 * @param defaultExtension The default file extension without the dot.
 * @param allowedExtensions The allowed file extensions without dots.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if cancelled.
 * @throws FileKitDialogException When a valid file-saving operation cannot be prepared, presented, or completed.
 * Invalid arguments and unsupported argument combinations remain caller-contract violations and are not wrapped in this type.
 */
public suspend fun FileKit.openFileSaver(
    suggestedName: String,
    defaultExtension: String? = null,
    allowedExtensions: Set<String>? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? = platformOpenFileSaver(
    suggestedName = suggestedName,
    defaultExtension = defaultExtension,
    allowedExtensions = allowedExtensions,
    directory = directory,
    dialogSettings = dialogSettings,
)

/**
 * Opens a file saver dialog.
 *
 * @param suggestedName The suggested name for the file.
 * @param extension The default file extension without the dot.
 * @param directory The initial directory. Supported on desktop platforms.
 * @param dialogSettings Platform-specific settings for the dialog.
 * @return The path where the file should be saved as a [PlatformFile], or null if cancelled.
 */
@Deprecated(
    message = "Use defaultExtension. The extension parameter is a default extension hint, not a filter.",
    replaceWith = ReplaceWith(
        "openFileSaver(" +
            "suggestedName = suggestedName, " +
            "defaultExtension = extension, " +
            "directory = directory, " +
            "dialogSettings = dialogSettings" +
            ")",
    ),
)
public suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? = openFileSaver(
    suggestedName = suggestedName,
    defaultExtension = extension,
    directory = directory,
    dialogSettings = dialogSettings,
)

internal expect suspend fun FileKit.platformOpenFileSaver(
    suggestedName: String,
    defaultExtension: String?,
    allowedExtensions: Set<String>?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile?

/**
 * Opens a file with the default application associated with its file type.
 *
 * @param file The file to open.
 * @param openFileSettings Platform-specific settings for opening the file.
 */
public expect fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings = FileKitOpenFileSettings.createDefault(),
)
