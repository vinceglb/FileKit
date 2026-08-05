@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WindowsDialogParentTest {
    @Test
    fun showWindowsDialog_withOwnerHandle_forwardsExactHwnd() {
        var shownParent: WinDef.HWND? = null

        val result = showWindowsDialog(-1) { hwnd ->
            shownParent = hwnd
            "shown"
        }

        assertEquals("shown", result)
        assertEquals(-1, Pointer.nativeValue(shownParent?.pointer))
    }

    @Test
    fun showWindowsDialog_withoutOwnerHandle_forwardsNull() {
        var shownParent: WinDef.HWND? = WinDef.HWND(Pointer(1))

        showWindowsDialog(parentHandle = null) { hwnd ->
            shownParent = hwnd
        }

        assertNull(shownParent)
    }
}
