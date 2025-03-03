package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.IUnknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.dialogs.platform.windows.util.GuidFixed


internal interface IShellItem : IUnknown {
    fun BindToHandler(
        pbc: Pointer?,
        bhid: Guid.GUID.ByReference?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?
    ): WinNT.HRESULT? // IBindCtx

    fun GetParent(
        ppsi: PointerByReference?
    ): WinNT.HRESULT? // IShellItem

    fun GetDisplayName(
        sigdnName: Long,
        ppszName: PointerByReference?
    ): WinNT.HRESULT? // SIGDN, WString

    fun GetAttributes(
        sfgaoMask: Int,
        psfgaoAttribs: IntByReference?
    ): WinNT.HRESULT? // SFGAOF, SFGAOF

    fun Compare(
        psi: Pointer?,
        hint: Int,
        piOrder: IntByReference?
    ): WinNT.HRESULT? // IShellItem , SICHINTF

    companion object {
        val IID_ISHELLITEM: GuidFixed.IID = GuidFixed.IID("{43826d1e-e718-42ee-bc55-a1e261c37bfe}") // Guid.IID
        val CLSID_SHELLITEM: GuidFixed.CLSID = GuidFixed.CLSID("{9ac9fbe1-e0a2-4ad6-b4ee-e212013ea917}") // Guid.CLSID
    }
}