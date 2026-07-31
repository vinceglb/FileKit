@file:Suppress("ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import platform.AppKit.NSModalResponseAbort
import platform.AppKit.NSModalResponseCancel
import platform.AppKit.NSModalResponseOK
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MacosDialogResponseTest {
    @Test
    fun resolveMacosDialogResponse_cancel_returnsNull() {
        assertNull(resolveMacosDialogResponse(NSModalResponseCancel) { "selection" })
    }

    @Test
    fun resolveMacosDialogResponse_ok_returnsSelection() {
        assertEquals("selection", resolveMacosDialogResponse(NSModalResponseOK) { "selection" })
    }

    @Test
    fun resolveMacosDialogResponse_okWithoutSelection_throwsDialogFailure() {
        assertFailsWith<FileKitDialogException> {
            resolveMacosDialogResponse<String>(NSModalResponseOK) { null }
        }
    }

    @Test
    fun resolveMacosDialogResponse_abort_throwsDialogFailure() {
        assertFailsWith<FileKitDialogException> {
            resolveMacosDialogResponse(NSModalResponseAbort) { "selection" }
        }
    }
}
