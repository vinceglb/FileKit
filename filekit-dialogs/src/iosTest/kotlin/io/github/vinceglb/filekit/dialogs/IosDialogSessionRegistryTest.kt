@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

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
            val session = IosDialogContinuationSession(Any(), sessions, continuation)
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
                val session = IosDialogContinuationSession(Any(), sessions, continuation)
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
                IosDialogContinuationSession(Any(), sessions, continuation)
            }
        }
        yield()
        assertEquals(1, sessions.size)

        job.cancelAndJoin()

        assertEquals(0, sessions.size)
    }
}
