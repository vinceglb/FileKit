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
