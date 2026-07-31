package io.github.vinceglb.filekit.dialogs.platform

internal class JvmDialogOperationException : IllegalStateException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
