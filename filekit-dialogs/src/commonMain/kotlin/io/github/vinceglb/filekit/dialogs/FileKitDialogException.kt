package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.exceptions.FileKitException

/**
 * An expected inability to complete a valid dialog operation because of platform or environmental conditions.
 */
public open class FileKitDialogException : FileKitException {
    public constructor(message: String) : super(message)

    public constructor(message: String, cause: Throwable) : super(message, cause)
}
