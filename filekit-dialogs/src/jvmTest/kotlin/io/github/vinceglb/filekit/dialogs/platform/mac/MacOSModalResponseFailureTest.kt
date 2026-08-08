@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.mac

import com.sun.jna.Callback
import com.sun.jna.CallbackReference
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.Foundation
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.ref.Reference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MacOSModalResponseFailureTest {
    @Test
    fun MacOSFilePicker_modalResponses_distinguishAbortFromCancel() {
        if (PlatformUtil.current != Platform.MacOS) return

        val javaExecutable = File(System.getProperty("java.home"), "bin/java")
        val process = ProcessBuilder(
            javaExecutable.absolutePath,
            "-cp",
            System.getProperty("java.class.path"),
            MacOSModalResponseFailureHarness::class.java.name,
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        assertEquals(0, process.waitFor(), output)
    }
}

internal object MacOSModalResponseFailureHarness {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val picker = MacOSFilePicker()
            val settings = FileKitDialogSettings()
            val failures = mutableListOf<String>()

            withSavePanelModalResponse(NS_MODAL_RESPONSE_ABORT) {
                verify("file-picker", failures) {
                    assertFailsWith<FileKitPickerException> {
                        picker.openFilePicker(
                            fileExtensions = null,
                            directory = null,
                            dialogSettings = settings,
                        )
                    }
                }
                verify("directory", failures) {
                    val failure = assertFailsWith<FileKitDialogException> {
                        picker.openDirectoryPicker(
                            directory = null,
                            dialogSettings = settings,
                        )
                    }
                    assertEquals(FileKitDialogException::class, failure::class)
                }
                verify("saver", failures) {
                    val failure = assertFailsWith<FileKitDialogException> {
                        picker.openFileSaver(
                            suggestedName = "document",
                            defaultExtension = "txt",
                            allowedExtensions = setOf("txt"),
                            directory = null,
                            dialogSettings = settings,
                        )
                    }
                    assertEquals(FileKitDialogException::class, failure::class)
                }
            }

            withSavePanelModalResponse(NS_MODAL_RESPONSE_CANCEL) {
                verify("file-picker cancel", failures) {
                    assertNull(
                        picker.openFilePicker(
                            fileExtensions = null,
                            directory = null,
                            dialogSettings = settings,
                        ),
                    )
                }
                verify("directory cancel", failures) {
                    assertNull(
                        picker.openDirectoryPicker(
                            directory = null,
                            dialogSettings = settings,
                        ),
                    )
                }
                verify("saver cancel", failures) {
                    assertNull(
                        picker.openFileSaver(
                            suggestedName = "document",
                            defaultExtension = "txt",
                            allowedExtensions = setOf("txt"),
                            directory = null,
                            dialogSettings = settings,
                        ),
                    )
                }
            }

            check(failures.isEmpty()) { failures.joinToString(separator = "\n") }
        }
    }

    private inline fun verify(
        operation: String,
        failures: MutableList<String>,
        block: () -> Unit,
    ) {
        runCatching(block).exceptionOrNull()?.let { failure ->
            failures += "$operation: ${failure.message}"
        }
    }

    private inline fun <T> withSavePanelModalResponse(
        response: Long,
        block: () -> T,
    ): T {
        val objc = NativeLibrary.getInstance("objc")
        val panelClass = checkNotNull(Foundation.getObjcClass("NSSavePanel"))
        val runModalSelector = checkNotNull(Foundation.createSelector("runModal"))
        val runModalMethod = checkNotNull(
            objc.getFunction("class_getInstanceMethod").invokePointer(
                arrayOf(panelClass, runModalSelector),
            ),
        )
        val replacement = RunModalCallback { _, _ -> response }
        val replacementPointer = CallbackReference.getFunctionPointer(replacement)
        val methodSetImplementation = objc.getFunction("method_setImplementation")
        val original = checkNotNull(
            methodSetImplementation.invokePointer(
                arrayOf(runModalMethod, replacementPointer),
            ),
        )

        try {
            return block()
        } finally {
            methodSetImplementation.invokePointer(arrayOf(runModalMethod, original))
            Reference.reachabilityFence(replacement)
        }
    }

    private fun interface RunModalCallback : Callback {
        fun invoke(self: Pointer?, selector: Pointer?): Long
    }

    private const val NS_MODAL_RESPONSE_CANCEL = 0L
    private const val NS_MODAL_RESPONSE_ABORT = -1001L
}
