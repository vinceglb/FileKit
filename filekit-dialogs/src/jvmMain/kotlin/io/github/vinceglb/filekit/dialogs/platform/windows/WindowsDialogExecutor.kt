package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.platform.win32.Ole32
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

internal interface WindowsComRuntime {
    fun initializeSta(): Int

    fun uninitialize()
}

internal object JnaWindowsComRuntime : WindowsComRuntime {
    override fun initializeSta(): Int = Ole32.INSTANCE
        .CoInitializeEx(
            null,
            Ole32.COINIT_APARTMENTTHREADED or Ole32.COINIT_DISABLE_OLE1DDE,
        ).toInt()

    override fun uninitialize() {
        Ole32.INSTANCE.CoUninitialize()
    }
}

internal object WindowsDialogThreadFactory : ThreadFactory {
    override fun newThread(runnable: Runnable): Thread = Thread(runnable, THREAD_NAME).apply {
        isDaemon = true
    }

    private const val THREAD_NAME = "FileKit-Windows-Dialog"
}

internal class WindowsDialogOperationalException(
    message: String,
) : RuntimeException(message)

internal class WindowsDialogExecutor(
    private val comRuntime: WindowsComRuntime,
    threadFactory: ThreadFactory = WindowsDialogThreadFactory,
) : AutoCloseable {
    private val dispatcher = Executors
        .newSingleThreadExecutor(threadFactory)
        .asCoroutineDispatcher()

    suspend fun <T> execute(block: () -> T): T = withContext(dispatcher) {
        val initializationResult = comRuntime.initializeSta()
        if (initializationResult != S_OK && initializationResult != S_FALSE) {
            throw WindowsDialogOperationalException(
                "CoInitializeEx failed with HRESULT 0x${initializationResult.toUInt().toString(16)}",
            )
        }

        try {
            block()
        } finally {
            comRuntime.uninitialize()
        }
    }

    override fun close() {
        dispatcher.close()
    }

    private companion object {
        const val S_OK = 0
        const val S_FALSE = 1
    }
}
