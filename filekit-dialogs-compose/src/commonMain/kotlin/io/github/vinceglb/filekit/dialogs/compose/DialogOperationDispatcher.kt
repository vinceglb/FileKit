package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal suspend fun <OperationResult> runDialogOperation(
    operation: suspend () -> OperationResult,
    onError: (FileKitDialogException) -> Unit,
    onResult: suspend (OperationResult) -> Unit,
) {
    val result = try {
        operation()
    } catch (failure: FileKitDialogException) {
        currentCoroutineContext().ensureActive()
        onError(failure)
        return
    }

    currentCoroutineContext().ensureActive()
    onResult(result)
}
