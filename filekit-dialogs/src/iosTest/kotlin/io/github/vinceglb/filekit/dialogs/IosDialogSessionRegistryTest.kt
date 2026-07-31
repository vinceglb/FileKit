@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IosDialogSessionRegistryTest {
    @Test
    fun resolveIosDialogPresenter_configuredPresenter_wins() {
        val configured = Any()

        val presenter = resolveIosDialogPresenter(
            configuredPresenter = configured,
            fallbackPresenter = { Any() },
        )

        assertSame(configured, presenter)
    }

    @Test
    fun resolveIosDialogPresenter_fallbackPresenter_isUsed() {
        val fallback = Any()

        val presenter = resolveIosDialogPresenter<Any>(
            configuredPresenter = null,
            fallbackPresenter = { fallback },
        )

        assertSame(fallback, presenter)
    }

    @Test
    fun resolveIosDialogPresenter_missingPresenter_throwsDialogFailure() {
        val failure = assertFailsWith<FileKitDialogException> {
            resolveIosDialogPresenter<Any>(
                configuredPresenter = null,
                fallbackPresenter = { null },
            )
        }

        assertTrue(failure.message.orEmpty().contains("view controller"))
    }

    @Test
    fun requireIosFileSaverTemporaryValue_missingValue_throwsOperationFailure() {
        assertFailsWith<FileKitDialogException> {
            requireIosFileSaverTemporaryValue<Any>(null, "temporary file URL")
        }
    }

    @Test
    fun requireIosFileSaverWrite_failure_throwsOperationFailure() {
        assertFailsWith<FileKitDialogException> {
            requireIosFileSaverWrite(succeeded = false)
        }
    }

    @Test
    fun requireIosCameraWrite_failure_throwsOperationFailure() {
        assertFailsWith<FileKitDialogException> {
            requireIosCameraWrite(succeeded = false)
        }
    }

    @Test
    fun IosDialogSessionRegistry_multipleSessions_areRetainedIndependently() {
        val sessions = IosDialogSessionRegistry<Any>()
        val first = Any()
        val second = Any()

        sessions.retain(first)
        sessions.retain(second)

        assertEquals(2, sessions.size)
        sessions.release(first)
        assertEquals(1, sessions.size)
        sessions.release(second)
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosDialogSessionRegistry_staleRelease_doesNotAffectOtherSessions() {
        val sessions = IosDialogSessionRegistry<Any>()
        val first = Any()
        val second = Any()

        sessions.retain(first)
        sessions.retain(second)
        sessions.release(first)
        sessions.release(first)

        assertEquals(1, sessions.size)
        assertSame(second, sessions.singleOrNull())
    }

    @Test
    fun IosDialogContinuationSession_duplicateCompletion_resumesOnce() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()

        val result = suspendCancellableCoroutine<String> { continuation ->
            val session = createTestContinuationSession(Any(), sessions, continuation) { it() }
            session.complete("first")
            session.complete("second")
        }

        assertEquals("first", result)
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosDialogContinuationSession_setupFailure_releasesSession() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()
        val defect = IllegalStateException("setup failed")

        val failure = assertFailsWith<IllegalStateException> {
            suspendCancellableCoroutine<Unit> { continuation ->
                val session = createTestContinuationSession(Any(), sessions, continuation) { it() }
                session.present { throw defect }
            }
        }

        assertSame(defect, failure)
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosDialogContinuationSession_cancellation_releasesSession() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()
        val job = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createTestContinuationSession(Any(), sessions, continuation) { it() }
            }
        }
        yield()
        assertEquals(1, sessions.size)

        job.cancelAndJoin()

        assertEquals(0, sessions.size)
    }

    @Test
    fun IosDialogContinuationSession_cancellation_cleansUpBeforeReleasingSession() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()
        var retainedDuringCleanup = false
        val job = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createTestContinuationSession(
                    session = Any(),
                    registry = sessions,
                    continuation = continuation,
                    onCancellation = { finishCleanup ->
                        retainedDuringCleanup = sessions.size == 1
                        finishCleanup()
                    },
                )
            }
        }
        yield()

        job.cancelAndJoin()

        assertTrue(retainedDuringCleanup)
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosDialogContinuationSession_cancellation_canDeferReleaseUntilCleanupCompletes() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()
        var finishCleanup: (() -> Unit)? = null
        val job = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createTestContinuationSession(
                    session = Any(),
                    registry = sessions,
                    continuation = continuation,
                    onCancellation = { finishCleanup = it },
                )
            }
        }
        yield()

        job.cancelAndJoin()

        assertEquals(1, sessions.size)
        finishCleanup?.invoke()
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosPresentedDialogSession_cancellation_dismissesBeforeReleasingSession() = runTest {
        val sessions = IosDialogSessionRegistry<Any>()
        val presentations = IosDialogPresentationRegistry()
        var retainedDuringDismissal = false
        val job = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createIosPresentedDialogSession(
                    session = Any(),
                    registry = sessions,
                    continuation = continuation,
                    presenter = Any(),
                    presentations = presentations,
                    overlapFailure = { FileKitDialogException("Presenter is already occupied.") },
                    dismiss = { finishDismissal ->
                        retainedDuringDismissal = sessions.size == 1
                        finishDismissal()
                    },
                )
            }
        }
        yield()

        job.cancelAndJoin()

        assertTrue(retainedDuringDismissal)
        assertEquals(0, sessions.size)
    }

    @Test
    fun IosPresentedDialogSession_samePresenter_rejectsOverlapAcrossSessionRegistries() = runTest {
        val presentations = IosDialogPresentationRegistry()
        val firstSessions = IosDialogSessionRegistry<Any>()
        val secondSessions = IosDialogSessionRegistry<Any>()
        val presenter = Any()
        val firstJob = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createIosPresentedDialogSession(
                    session = Any(),
                    registry = firstSessions,
                    continuation = continuation,
                    presenter = presenter,
                    presentations = presentations,
                    overlapFailure = { FileKitDialogException("Presenter is already occupied.") },
                    dismiss = { it() },
                )
            }
        }
        yield()

        assertFailsWith<FileKitDialogException> {
            suspendCancellableCoroutine<Unit> { continuation ->
                createIosPresentedDialogSession(
                    session = Any(),
                    registry = secondSessions,
                    continuation = continuation,
                    presenter = presenter,
                    presentations = presentations,
                    overlapFailure = { FileKitDialogException("Presenter is already occupied.") },
                    dismiss = { it() },
                )
            }
        }

        assertEquals(1, firstSessions.size)
        assertEquals(0, secondSessions.size)
        firstJob.cancelAndJoin()
        assertEquals(0, firstSessions.size)

        val result = suspendCancellableCoroutine<String> { continuation ->
            val session = createIosPresentedDialogSession(
                session = Any(),
                registry = secondSessions,
                continuation = continuation,
                presenter = presenter,
                presentations = presentations,
                overlapFailure = { FileKitDialogException("Presenter is already occupied.") },
                dismiss = { it() },
            )
            session.complete("completed")
        }
        assertEquals("completed", result)
        assertEquals(0, secondSessions.size)
    }

    @Test
    fun IosPresentedDialogSession_differentPresenters_allowConcurrentSessions() = runTest {
        val presentations = IosDialogPresentationRegistry()
        val sessions = IosDialogSessionRegistry<Any>()

        fun launchSession(presenter: Any) = launch {
            suspendCancellableCoroutine<Unit> { continuation ->
                createIosPresentedDialogSession(
                    session = Any(),
                    registry = sessions,
                    continuation = continuation,
                    presenter = presenter,
                    presentations = presentations,
                    overlapFailure = { FileKitDialogException("Presenter is already occupied.") },
                    dismiss = { it() },
                )
            }
        }

        val firstJob = launchSession(Any())
        val secondJob = launchSession(Any())
        yield()
        assertEquals(2, sessions.size)

        firstJob.cancelAndJoin()
        assertEquals(1, sessions.size)
        secondJob.cancelAndJoin()
        assertEquals(0, sessions.size)
    }
}

private fun <Session : Any, Result> createTestContinuationSession(
    session: Session,
    registry: IosDialogSessionRegistry<Session>,
    continuation: CancellableContinuation<Result>,
    onCancellation: (finishCleanup: () -> Unit) -> Unit,
): IosDialogContinuationSession<Session, Result> = IosDialogContinuationSession(
    session = session,
    registry = registry,
    continuation = continuation,
    onCancellation = onCancellation,
    releasePresentation = {},
)
