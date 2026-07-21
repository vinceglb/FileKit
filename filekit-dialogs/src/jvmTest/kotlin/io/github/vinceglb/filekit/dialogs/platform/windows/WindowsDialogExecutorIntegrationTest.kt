package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.WTypes
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.FileOpenDialog
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.IFileOpenDialog
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@Suppress("ktlint:standard:function-naming", "FunctionName")
class WindowsDialogExecutorIntegrationTest {
    @Test
    fun WindowsDialogExecutor_executeFromMtaCaller_createsFileDialogOnOwnedStaThread() = runBlocking {
        if (PlatformUtil.current != Platform.Windows) return@runBlocking

        val callerDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        try {
            withContext(callerDispatcher) {
                val callerThread = Thread.currentThread()
                val callerInitialization = Ole32.INSTANCE
                    .CoInitializeEx(null, Ole32.COINIT_MULTITHREADED)
                    .toInt()
                assertEquals(S_OK, callerInitialization)

                try {
                    val dialogExecutor = WindowsDialogExecutor(JnaWindowsComRuntime)
                    try {
                        val dialogThread = dialogExecutor.execute {
                            val dialogPointer = PointerByReference()
                            val creationResult = Ole32.INSTANCE
                                .CoCreateInstance(
                                    IFileOpenDialog.CLSID_FILEOPENDIALOG,
                                    null,
                                    WTypes.CLSCTX_ALL,
                                    IFileOpenDialog.IID_IFILEOPENDIALOG,
                                    dialogPointer,
                                ).toInt()

                            assertEquals(S_OK, creationResult)
                            val pointer = assertNotNull(dialogPointer.value)
                            val fileDialog = FileOpenDialog(pointer)
                            try {
                                Thread.currentThread()
                            } finally {
                                fileDialog.Release()
                            }
                        }

                        assertNotEquals(callerThread, dialogThread)
                        assertEquals("FileKit-Windows-Dialog", dialogThread.name)
                    } finally {
                        dialogExecutor.close()
                    }
                } finally {
                    Ole32.INSTANCE.CoUninitialize()
                }
            }
        } finally {
            callerDispatcher.close()
        }
    }

    private companion object {
        const val S_OK = 0
    }
}
