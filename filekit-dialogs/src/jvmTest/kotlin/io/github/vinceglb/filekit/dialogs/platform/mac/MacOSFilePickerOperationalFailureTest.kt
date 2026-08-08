@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.mac

import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.platform.mac.foundation.Foundation
import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MacOSFilePickerOperationalFailureTest {
    @Test
    fun MacOSFilePicker_runnableClassCollision_exposesOperationAppropriateFailuresWithCause() {
        if (PlatformUtil.current != Platform.MacOS) return

        val javaExecutable = File(System.getProperty("java.home"), "bin/java")
        val process = ProcessBuilder(
            javaExecutable.absolutePath,
            "-cp",
            System.getProperty("java.class.path"),
            MacOSFilePickerOperationalFailureHarness::class.java.name,
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        assertEquals(0, process.waitFor(), output)
    }

    @Test
    fun MacOSFilePicker_incompatibleParent_remainsInvalidInvocation() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            MacOSFilePicker().openDirectoryPicker(
                directory = null,
                dialogSettings = FileKitDialogSettings(
                    parent = FileKitDialogParent.windows(1),
                ),
            )
        }
    }

    @Test
    fun MacOSFilePicker_cancellationDuringOperation_remainsUnwrapped() {
        val cancellation = CancellationException("Cancelled")

        val failure = assertFailsWith<CancellationException> {
            normalizeRunnableBootstrapFailure(
                operationalFailure = { cause -> FileKitDialogException("Operational failure", cause) },
            ) {
                throw cancellation
            }
        }

        assertSame(cancellation, failure)
    }

    @Test
    fun MacOSFilePicker_unexpectedFailureDuringOperation_remainsUnwrapped() {
        val unexpectedFailure = IllegalStateException("Unexpected failure")

        val failure = assertFailsWith<IllegalStateException> {
            normalizeRunnableBootstrapFailure(
                operationalFailure = { cause -> FileKitDialogException("Operational failure", cause) },
            ) {
                throw unexpectedFailure
            }
        }

        assertSame(unexpectedFailure, failure)
    }
}

internal object MacOSFilePickerOperationalFailureHarness {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            registerForeignRunnableClass()
            val picker = MacOSFilePicker()
            val settings = FileKitDialogSettings()

            val pickerFailure = assertFailsWith<FileKitPickerException> {
                picker.openFilePicker(
                    fileExtensions = null,
                    directory = null,
                    dialogSettings = settings,
                )
            }
            assertIs<IllegalStateException>(pickerFailure.cause)
            assertTrue(pickerFailure.cause?.message?.contains(RUNNABLE_ADAPTER_CLASS_NAME) == true)

            val directoryFailure = assertFailsWith<FileKitDialogException> {
                picker.openDirectoryPicker(
                    directory = null,
                    dialogSettings = settings,
                )
            }
            assertIs<IllegalStateException>(directoryFailure.cause)
            assertTrue(directoryFailure.cause?.message?.contains(RUNNABLE_ADAPTER_CLASS_NAME) == true)

            val saverFailure = assertFailsWith<FileKitDialogException> {
                picker.openFileSaver(
                    suggestedName = "document",
                    defaultExtension = "txt",
                    allowedExtensions = setOf("txt"),
                    directory = null,
                    dialogSettings = settings,
                )
            }
            assertIs<IllegalStateException>(saverFailure.cause)
            assertTrue(saverFailure.cause?.message?.contains(RUNNABLE_ADAPTER_CLASS_NAME) == true)
        }
    }

    private fun registerForeignRunnableClass() {
        val nsObject = Foundation.getObjcClass("NSObject")
        check(!Foundation.isNil(nsObject)) {
            "Unable to resolve NSObject while preparing the runnable adapter collision"
        }

        val foreignClass = Foundation.allocateObjcClassPair(nsObject, RUNNABLE_ADAPTER_CLASS_NAME)
        check(!Foundation.isNil(foreignClass)) {
            "Unable to allocate the foreign $RUNNABLE_ADAPTER_CLASS_NAME class"
        }

        Foundation.registerObjcClassPair(foreignClass)
    }

    private const val RUNNABLE_ADAPTER_CLASS_NAME = "FileKitMainThreadRunnable"
}
