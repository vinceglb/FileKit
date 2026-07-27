package io.github.vinceglb.filekit.dialogs.platform.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.resolveWindowsHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WindowsDialogParentTest {
    @Test
    fun showWindowsDialog_passesResolvedHwndToShow() {
        var shownParent: WinDef.HWND? = null
        val parentHandle = FileKitDialogParent.windows(0x1234).resolveWindowsHandle {
            error("Unexpected AWT conversion")
        }

        val result = showWindowsDialog(parentHandle) { hwnd ->
            shownParent = hwnd
            "shown"
        }

        assertEquals("shown", result)
        assertEquals(0x1234, Pointer.nativeValue(shownParent?.pointer))
    }

    @Test
    fun showWindowsDialog_passesNullWhenUnparented() {
        var shownParent: WinDef.HWND? = WinDef.HWND(Pointer(1))

        showWindowsDialog(parentHandle = null) { hwnd ->
            shownParent = hwnd
        }

        assertNull(shownParent)
    }
}
