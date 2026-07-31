package io.github.vinceglb.filekit.dialogs

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

internal class IosDialogSessionRegistry<Session : Any> {
    private val sessions = mutableSetOf<Session>()

    val size: Int
        get() = sessions.size

    fun retain(session: Session) {
        check(sessions.add(session)) { "The dialog session is already retained." }
    }

    fun release(session: Session) {
        sessions.remove(session)
    }

    fun singleOrNull(): Session? = sessions.singleOrNull()
}

internal class IosDialogContinuationSession<Session : Any, Result>(
    private val session: Session,
    private val registry: IosDialogSessionRegistry<Session>,
    private val continuation: CancellableContinuation<Result>,
) {
    private var completed = false

    init {
        registry.retain(session)
        continuation.invokeOnCancellation { release() }
    }

    fun complete(result: Result) {
        if (completed || !continuation.isActive) return
        completed = true
        release()
        continuation.resume(result)
    }

    inline fun present(block: () -> Unit) {
        try {
            block()
        } catch (cause: Throwable) {
            release()
            throw cause
        }
    }

    fun release() {
        registry.release(session)
    }
}

internal inline fun <Presenter : Any> resolveIosDialogPresenter(
    configuredPresenter: Presenter?,
    fallbackPresenter: () -> Presenter?,
    failure: () -> FileKitDialogException = {
        FileKitDialogException("No view controller is available to present the dialog.")
    },
): Presenter = configuredPresenter ?: fallbackPresenter() ?: throw failure()

internal fun <Value : Any> requireIosFileSaverTemporaryValue(
    value: Value?,
    description: String,
): Value = value ?: throw FileKitDialogException("Failed to create the $description for the file saver.")

internal fun requireIosFileSaverWrite(succeeded: Boolean) {
    if (!succeeded) throw FileKitDialogException("Failed to create the temporary file for the file saver.")
}

internal fun requireIosCameraWrite(succeeded: Boolean) {
    if (!succeeded) throw FileKitDialogException("Failed to write the captured image to its destination.")
}
