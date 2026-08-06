package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitDialogException

internal suspend fun <OperationResult> runDialogOperation(
    operation: suspend () -> OperationResult,
    onError: (FileKitDialogException) -> Unit,
    onResult: suspend (OperationResult) -> Unit,
) {
    val result = try {
        operation()
    } catch (failure: FileKitDialogException) {
        onError(failure)
        return
    }

    onResult(result)
}
