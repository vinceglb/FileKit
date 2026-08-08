@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.test.runTest
import platform.AppKit.NSModalResponse
import platform.AppKit.NSModalResponseAbort
import platform.AppKit.NSModalResponseCancel
import platform.Foundation.NSClassFromString
import platform.Foundation.NSSelectorFromString
import platform.objc.class_getInstanceMethod
import platform.objc.method_setImplementation
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class MacOSModalResponseFailureTest {
    @Test
    fun FilePicker_abortedPanel_throwsPickerOperationalFailure() = runTest {
        withSavePanelModalResponse(NSModalResponseAbort) {
            assertFailsWith<FileKitPickerException> {
                FileKit.openFilePicker()
            }
        }
    }

    @Test
    fun DirectoryPicker_abortedPanel_throwsDialogOperationalFailure() = runTest {
        withSavePanelModalResponse(NSModalResponseAbort) {
            assertFailsWith<FileKitDialogException> {
                FileKit.openDirectoryPicker()
            }
        }
    }

    @Test
    fun FileSaver_abortedPanel_throwsDialogOperationalFailure() = runTest {
        withSavePanelModalResponse(NSModalResponseAbort) {
            assertFailsWith<FileKitDialogException> {
                FileKit.openFileSaver(
                    suggestedName = "example",
                    defaultExtension = null,
                    allowedExtensions = null,
                )
            }
        }
    }

    @Test
    fun FilePicker_cancelledPanel_returnsNull() = runTest {
        withSavePanelModalResponse(NSModalResponseCancel) {
            assertNull(FileKit.openFilePicker())
        }
    }

    @Test
    fun DirectoryPicker_cancelledPanel_returnsNull() = runTest {
        withSavePanelModalResponse(NSModalResponseCancel) {
            assertNull(FileKit.openDirectoryPicker())
        }
    }

    @Test
    fun FileSaver_cancelledPanel_returnsNull() = runTest {
        withSavePanelModalResponse(NSModalResponseCancel) {
            assertNull(
                FileKit.openFileSaver(
                    suggestedName = "example",
                    defaultExtension = null,
                    allowedExtensions = null,
                ),
            )
        }
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private inline fun <Result> withSavePanelModalResponse(
    response: NSModalResponse,
    block: () -> Result,
): Result {
    val replacement = when (response) {
        NSModalResponseAbort -> abortRunModalImplementation
        NSModalResponseCancel -> cancelRunModalImplementation
        else -> error("Unsupported intercepted modal response: $response")
    }
    val panelClass = checkNotNull(NSClassFromString("NSSavePanel"))
    val runModalSelector = NSSelectorFromString("runModal")
    val runModalMethod = checkNotNull(class_getInstanceMethod(panelClass, runModalSelector))
    val original = method_setImplementation(runModalMethod, replacement.reinterpret())

    try {
        return block()
    } finally {
        method_setImplementation(runModalMethod, original)
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private val abortRunModalImplementation:
    CPointer<CFunction<(COpaquePointer?, COpaquePointer?) -> NSModalResponse>> =
    staticCFunction { _, _ -> NSModalResponseAbort }

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private val cancelRunModalImplementation:
    CPointer<CFunction<(COpaquePointer?, COpaquePointer?) -> NSModalResponse>> =
    staticCFunction { _, _ -> NSModalResponseCancel }
