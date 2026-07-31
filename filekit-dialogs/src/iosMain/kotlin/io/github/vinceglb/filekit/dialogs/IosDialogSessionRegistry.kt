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

internal class IosDialogPresentationRegistry {
    private class Presentation(
        val presenter: Any,
    )

    private val presentations = mutableSetOf<Presentation>()

    fun retain(
        presenter: Any,
        overlapFailure: () -> FileKitDialogException,
    ): () -> Unit {
        if (presentations.any { it.presenter === presenter }) throw overlapFailure()

        val presentation = Presentation(presenter)
        presentations.add(presentation)
        return { presentations.remove(presentation) }
    }
}

internal class IosDialogContinuationSession<Session : Any, Result>(
    private val session: Session,
    private val registry: IosDialogSessionRegistry<Session>,
    private val continuation: CancellableContinuation<Result>,
    private val onCancellation: (finishCleanup: () -> Unit) -> Unit,
    private val releasePresentation: () -> Unit,
) {
    private var completed = false

    init {
        registry.retain(session)
        continuation.invokeOnCancellation { onCancellation(::release) }
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
        releasePresentation()
    }
}

internal fun <Session : Any, Result> createIosPresentedDialogSession(
    session: Session,
    registry: IosDialogSessionRegistry<Session>,
    continuation: CancellableContinuation<Result>,
    presenter: Any,
    presentations: IosDialogPresentationRegistry,
    overlapFailure: () -> FileKitDialogException,
    dismiss: (finishDismissal: () -> Unit) -> Unit,
): IosDialogContinuationSession<Session, Result> {
    val releasePresentation = presentations.retain(presenter, overlapFailure)
    return try {
        IosDialogContinuationSession(
            session = session,
            registry = registry,
            continuation = continuation,
            onCancellation = dismiss,
            releasePresentation = releasePresentation,
        )
    } catch (cause: Throwable) {
        releasePresentation()
        throw cause
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
