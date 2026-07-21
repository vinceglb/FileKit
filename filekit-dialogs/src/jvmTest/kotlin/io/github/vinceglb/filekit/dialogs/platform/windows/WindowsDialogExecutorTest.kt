package io.github.vinceglb.filekit.dialogs.platform.windows

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ktlint:standard:function-naming", "FunctionName")
class WindowsDialogExecutorTest {
    @Test
    fun WindowsDialogExecutor_executeAfterSuccessfulInitialization_balancesCom() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_OK)
        val executor = WindowsDialogExecutor(comRuntime)

        try {
            val result = executor.execute { "selected.txt" }

            assertEquals("selected.txt", result)
            assertEquals(1, comRuntime.initializeCalls)
            assertEquals(1, comRuntime.uninitializeCalls)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_executeWhenComWasAlreadyInitialized_balancesCom() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_FALSE)
        val executor = WindowsDialogExecutor(comRuntime)

        try {
            val result = executor.execute { "selected.txt" }

            assertEquals("selected.txt", result)
            assertEquals(1, comRuntime.initializeCalls)
            assertEquals(1, comRuntime.uninitializeCalls)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_execute_usesNamedDaemonThread() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_OK)
        val executor = WindowsDialogExecutor(comRuntime)

        try {
            val worker = executor.execute { Thread.currentThread() }

            assertEquals("FileKit-Windows-Dialog", worker.name)
            assertTrue(worker.isDaemon)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_executeWhenApartmentModeChanged_doesNotRunOrUninitialize() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = RPC_E_CHANGED_MODE)
        val executor = WindowsDialogExecutor(comRuntime)
        var operationCalled = false

        try {
            val exception = assertFailsWith<RuntimeException> {
                executor.execute {
                    operationCalled = true
                }
            }

            assertFalse(operationCalled)
            assertEquals(1, comRuntime.initializeCalls)
            assertEquals(0, comRuntime.uninitializeCalls)
            assertEquals("CoInitializeEx failed with HRESULT 0x80010106", exception.message)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_executeWhenInitializationFails_doesNotRunOrUninitialize() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = E_OUTOFMEMORY)
        val executor = WindowsDialogExecutor(comRuntime)
        var operationCalled = false

        try {
            val exception = assertFailsWith<RuntimeException> {
                executor.execute {
                    operationCalled = true
                }
            }

            assertFalse(operationCalled)
            assertEquals(1, comRuntime.initializeCalls)
            assertEquals(0, comRuntime.uninitializeCalls)
            assertEquals("CoInitializeEx failed with HRESULT 0x8007000e", exception.message)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_executeWhenOperationFails_balancesCom() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_OK)
        val executor = WindowsDialogExecutor(comRuntime)

        try {
            assertFailsWith<ExpectedException> {
                executor.execute { throw ExpectedException() }
            }

            assertEquals(1, comRuntime.initializeCalls)
            assertEquals(1, comRuntime.uninitializeCalls)
        } finally {
            executor.close()
        }
    }

    @Test
    fun WindowsDialogExecutor_executeAfterClose_rejectsOperation() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_OK)
        val executor = WindowsDialogExecutor(comRuntime)
        executor.close()

        val exception = assertFailsWith<CancellationException> {
            executor.execute { "selected.txt" }
        }

        var cause: Throwable? = exception
        while (cause != null && cause !is RejectedExecutionException) {
            cause = cause.cause
        }
        assertTrue(cause is RejectedExecutionException)

        assertEquals(0, comRuntime.initializeCalls)
        assertEquals(0, comRuntime.uninitializeCalls)
    }

    @Test
    fun WindowsDialogExecutor_executeConcurrently_serializesOnOwnedThread() = runBlocking {
        val comRuntime = FakeWindowsComRuntime(initializationResult = S_OK)
        val executor = WindowsDialogExecutor(comRuntime)
        val firstStarted = CountDownLatch(1)
        val releaseFirst = CountDownLatch(1)
        val secondStarted = CountDownLatch(1)

        try {
            val first = async(Dispatchers.Default) {
                executor.execute {
                    firstStarted.countDown()
                    assertTrue(releaseFirst.await(5, TimeUnit.SECONDS))
                    Thread.currentThread()
                }
            }
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS))

            val second = async(Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
                executor.execute {
                    secondStarted.countDown()
                    Thread.currentThread()
                }
            }
            assertFalse(secondStarted.await(100, TimeUnit.MILLISECONDS))

            releaseFirst.countDown()
            val firstThread = first.await()
            val secondThread = second.await()

            assertEquals(firstThread, secondThread)
            assertTrue(secondStarted.await(5, TimeUnit.SECONDS))
        } finally {
            releaseFirst.countDown()
            executor.close()
        }
    }

    private class FakeWindowsComRuntime(
        private val initializationResult: Int,
    ) : WindowsComRuntime {
        var initializeCalls = 0
            private set

        var uninitializeCalls = 0
            private set

        override fun initializeSta(): Int {
            initializeCalls += 1
            return initializationResult
        }

        override fun uninitialize() {
            uninitializeCalls += 1
        }
    }

    private class ExpectedException : RuntimeException()

    private companion object {
        const val S_OK = 0
        const val S_FALSE = 1
        val RPC_E_CHANGED_MODE = 0x80010106u.toInt()
        val E_OUTOFMEMORY = 0x8007000Eu.toInt()
    }
}
