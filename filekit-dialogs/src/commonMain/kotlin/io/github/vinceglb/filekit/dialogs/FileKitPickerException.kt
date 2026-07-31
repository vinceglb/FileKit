package io.github.vinceglb.filekit.dialogs

public class FileKitPickerException : FileKitDialogException {
    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}
