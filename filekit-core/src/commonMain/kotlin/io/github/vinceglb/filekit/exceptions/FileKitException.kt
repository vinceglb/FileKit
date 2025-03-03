package io.github.vinceglb.filekit.exceptions

public open class FileKitException : Exception {
    public constructor(message: String) : super(message)
    public constructor(message: String, cause: Throwable) : super(message, cause)
}
