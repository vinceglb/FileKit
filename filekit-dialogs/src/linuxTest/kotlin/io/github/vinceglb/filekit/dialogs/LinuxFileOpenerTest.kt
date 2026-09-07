@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:function-naming")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.exceptions.FileKitException
import platform.posix.ECHILD
import platform.posix.ESRCH
import platform.posix.WNOHANG
import platform.posix.errno
import platform.posix.kill
import platform.posix.usleep
import platform.posix.waitpid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinuxFileOpenerTest {
    @Test
    fun LinuxOpener_relativePaths_areUnambiguousFileArguments() {
        assertEquals("./-report.pdf", xdgOpenFileArgument("-report.pdf"))
        assertEquals("./https:report.pdf", xdgOpenFileArgument("https:report.pdf"))
        assertEquals("./folder/report.pdf", xdgOpenFileArgument("folder/report.pdf"))
        assertEquals("/tmp/report.pdf", xdgOpenFileArgument("/tmp/report.pdf"))
    }

    @Test
    fun LinuxProcess_childExits_reapsWithoutBlockingCaller() {
        val pid = spawnLinuxProcess("/bin/sleep", listOf("1"))
        assertEquals(0, kill(pid, 0), "The opener should return while its child is running")

        // kill(pid, 0) still succeeds for a zombie, so this observes reaping without reaping it
        // ourselves and accidentally hiding a regression in the background reaper.
        for (attempt in 0 until 500) {
            if (kill(pid, 0) < 0 && errno == ESRCH) break
            usleep(10_000u)
        }
        assertEquals(-1, kill(pid, 0), "The exited child must not remain as a zombie")
        assertEquals(ESRCH, errno)
        assertEquals(-1, waitpid(pid, null, WNOHANG))
        assertEquals(ECHILD, errno, "The opener must have collected the child's exit status")
    }

    @Test
    fun LinuxProcess_missingExecutable_reportsSpawnFailure() {
        assertFailsWith<FileKitException> {
            spawnLinuxProcess("/filekit-nonexistent-directory/xdg-open", emptyList())
        }
    }
}
