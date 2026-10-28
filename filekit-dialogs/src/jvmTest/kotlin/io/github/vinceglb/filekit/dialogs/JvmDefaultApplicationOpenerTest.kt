@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JvmDefaultApplicationOpenerTest {
    private val file = File("report.pdf")

    @Test
    fun JvmDefaultApplicationOpener_desktopSupported_usesDesktopOnly() {
        val opened = mutableListOf<File>()
        val commands = mutableListOf<List<String>>()
        val opener = opener(
            desktopSupported = true,
            isLinux = true,
            opened = opened,
            commands = commands,
        )

        opener.open(file)

        assertEquals(listOf(file), opened)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun JvmDefaultApplicationOpener_desktopError_isNotWrapped() {
        val opener = JvmDefaultApplicationOpener(
            isDesktopOpenSupported = { true },
            desktopOpen = { throw IOException("No application") },
            isLinux = true,
            startProcess = { error("xdg-open must not run") },
        )

        assertFailsWith<IOException> { opener.open(file) }
    }

    @Test
    fun JvmDefaultApplicationOpener_desktopUnsupportedOnLinux_runsXdgOpenWithAbsolutePath() {
        val commands = mutableListOf<List<String>>()
        val opener = opener(
            isLinux = true,
            commands = commands,
            process = FakeProcess(exitCode = 0),
        )

        opener.open(file)

        assertEquals(listOf(listOf("xdg-open", file.absolutePath)), commands)
    }

    @Test
    fun JvmDefaultApplicationOpener_xdgOpenStillRunning_returnsWithoutWaitingForever() {
        val process = FakeProcess(exitCode = null)
        val opener = opener(isLinux = true, process = process)

        opener.open(file)

        assertEquals(TIMEOUT_MILLIS, process.waitedMillis)
    }

    @Test
    fun JvmDefaultApplicationOpener_xdgOpenNonZeroExit_throwsDialogException() {
        val opener = opener(isLinux = true, process = FakeProcess(exitCode = 3))

        val exception = assertFailsWith<FileKitDialogException> { opener.open(file) }

        assertTrue(exception.message!!.contains("xdg-open exited with code 3"))
    }

    @Test
    fun JvmDefaultApplicationOpener_xdgOpenMissing_throwsDialogExceptionWithCause() {
        val opener = JvmDefaultApplicationOpener(
            isDesktopOpenSupported = { false },
            desktopOpen = { error("Desktop must not be used") },
            isLinux = true,
            startProcess = { throw IOException("Cannot run program \"xdg-open\"") },
        )

        val exception = assertFailsWith<FileKitDialogException> { opener.open(file) }

        assertIs<IOException>(exception.cause)
    }

    @Test
    fun JvmDefaultApplicationOpener_desktopUnsupportedOutsideLinux_throwsDialogException() {
        val commands = mutableListOf<List<String>>()
        val opener = opener(isLinux = false, commands = commands)

        assertFailsWith<FileKitDialogException> { opener.open(file) }
        assertTrue(commands.isEmpty())
    }

    private fun opener(
        desktopSupported: Boolean = false,
        isLinux: Boolean,
        opened: MutableList<File> = mutableListOf(),
        commands: MutableList<List<String>> = mutableListOf(),
        process: Process = FakeProcess(exitCode = 0),
    ) = JvmDefaultApplicationOpener(
        isDesktopOpenSupported = { desktopSupported },
        desktopOpen = { opened += it },
        isLinux = isLinux,
        startProcess = { command ->
            commands += command
            process
        },
        exitTimeoutMillis = TIMEOUT_MILLIS,
    )

    /** A process that has exited with [exitCode], or is still running when [exitCode] is null. */
    private class FakeProcess(
        private val exitCode: Int?,
    ) : Process() {
        var waitedMillis: Long? = null

        override fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
            waitedMillis = unit.toMillis(timeout)
            return exitCode != null
        }

        override fun waitFor(): Int = exitCode ?: error("Unbounded wait")

        override fun exitValue(): Int = exitCode ?: throw IllegalThreadStateException()

        override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()

        override fun getInputStream(): InputStream = InputStream.nullInputStream()

        override fun getErrorStream(): InputStream = InputStream.nullInputStream()

        override fun destroy() = Unit
    }

    private companion object {
        const val TIMEOUT_MILLIS = 50L
    }
}
