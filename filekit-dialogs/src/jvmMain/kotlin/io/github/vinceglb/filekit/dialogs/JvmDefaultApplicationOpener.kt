package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

internal const val XDG_OPEN_EXIT_TIMEOUT_MILLIS: Long = 3_000

/**
 * Opens files with the default application on the JVM.
 *
 * Uses `java.awt.Desktop` when it can open files. Otherwise, on Linux, starts `xdg-open` and waits
 * a bounded time for it to report an error. Throws [FileKitDialogException] when neither works.
 */
internal class JvmDefaultApplicationOpener(
    private val isDesktopOpenSupported: () -> Boolean,
    private val desktopOpen: (File) -> Unit,
    private val isLinux: Boolean,
    private val startProcess: (List<String>) -> Process,
    private val exitTimeoutMillis: Long = XDG_OPEN_EXIT_TIMEOUT_MILLIS,
) {
    fun open(file: File) {
        when {
            isDesktopOpenSupported() -> desktopOpen(file)

            isLinux -> openWithXdgOpen(file)

            else -> throw FileKitDialogException(
                "Could not open the file with the default application: " +
                    "java.awt.Desktop cannot open files in this environment.",
            )
        }
    }

    private fun openWithXdgOpen(file: File) {
        val process = try {
            // An absolute path keeps xdg-open from reading the argument as an option or a URI scheme.
            startProcess(listOf("xdg-open", file.absolutePath))
        } catch (e: IOException) {
            throw FileKitDialogException(
                "Could not open the file with the default application: " +
                    "java.awt.Desktop cannot open files and xdg-open could not be started.",
                e,
            )
        }

        val exited = try {
            process.waitFor(exitTimeoutMillis, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            return
        }

        // Some xdg-open fallbacks keep running until the viewer closes: treat that as success.
        if (exited && process.exitValue() != 0) {
            throw FileKitDialogException(
                "Could not open the file with the default application: " +
                    "xdg-open exited with code ${process.exitValue()}.",
            )
        }
    }

    companion object {
        val Default: JvmDefaultApplicationOpener = JvmDefaultApplicationOpener(
            isDesktopOpenSupported = {
                Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)
            },
            desktopOpen = { Desktop.getDesktop().open(it) },
            isLinux = PlatformUtil.current == Platform.Linux,
            startProcess = { command ->
                ProcessBuilder(command)
                    .redirectInput(ProcessBuilder.Redirect.from(File("/dev/null")))
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
            },
        )
    }
}
