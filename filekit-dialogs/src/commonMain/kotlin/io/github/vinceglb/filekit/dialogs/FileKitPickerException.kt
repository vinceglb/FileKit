package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.exceptions.FileKitException

public class FileKitPickerException : FileKitException {
    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}
