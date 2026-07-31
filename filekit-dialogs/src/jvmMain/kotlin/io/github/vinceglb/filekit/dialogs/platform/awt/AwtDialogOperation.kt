package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.platform.JvmDialogOperationException
import java.awt.HeadlessException

internal inline fun <Result> runAwtDialogOperation(
    message: String,
    block: () -> Result,
): Result = try {
    block()
} catch (cause: HeadlessException) {
    throw JvmDialogOperationException(message, cause)
}

internal inline fun <Result> dispatchAwtDialogOperation(
    isActive: () -> Boolean,
    operation: () -> Result,
    onResult: (Result) -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    if (!isActive()) return

    try {
        val result = operation()
        if (isActive()) onResult(result)
    } catch (failure: Throwable) {
        if (isActive()) onFailure(failure)
    }
}
