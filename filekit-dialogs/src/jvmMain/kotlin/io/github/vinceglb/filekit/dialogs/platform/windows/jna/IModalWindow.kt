package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.platform.win32.COM.IUnknown
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import io.github.vinceglb.filekit.dialogs.platform.windows.util.GuidFixed


internal interface IModalWindow : IUnknown {
    fun Show(hwndOwner: WinDef.HWND?): WinNT.HRESULT?

    companion object {
        val IID_IMODALWINDOW: GuidFixed.IID = GuidFixed.IID("{b4db1657-70d7-485e-8e3e-6fcb5a5c1802}") // Guid.IID
    }
}
