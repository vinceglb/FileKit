package io.github.vinceglb.filekit.dialogs

/**
 * An operational failure while opening or resolving a file-picker result.
 */
public class FileKitPickerException : FileKitDialogException {
    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}

internal const val WINDOWS_FILE_PICKER_FAILURE_MESSAGE: String =
    "The Windows file picker could not complete the operation."

internal const val WINDOWS_DIRECTORY_PICKER_FAILURE_MESSAGE: String =
    "The Windows directory picker could not complete the operation."

internal const val WINDOWS_FILE_SAVER_FAILURE_MESSAGE: String =
    "The Windows file saver could not complete the operation."

internal const val MACOS_FILE_PICKER_FAILURE_MESSAGE: String =
    "The macOS file picker could not complete the operation."

internal const val MACOS_DIRECTORY_PICKER_FAILURE_MESSAGE: String =
    "The macOS directory picker could not complete the operation."

internal const val MACOS_FILE_SAVER_FAILURE_MESSAGE: String =
    "The macOS file saver could not complete the operation."
