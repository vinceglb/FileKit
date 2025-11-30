package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.IUnknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.GETPROPERTYSTOREFLAGS
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.PROPERTYKEY
import io.github.vinceglb.filekit.dialogs.platform.windows.util.GuidFixed

@Suppress("ktlint:standard:function-naming", "FunctionName")
internal interface IShellItemArray : IUnknown {
    fun BindToHandler(
        pbc: Pointer?,
        bhid: Guid.GUID.ByReference?,
        riid: Guid.REFIID?,
        ppvOut: PointerByReference?,
    ): WinNT.HRESULT? // IBindCtx

    fun GetPropertyStore(
        flags: GETPROPERTYSTOREFLAGS?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?,
    ): WinNT.HRESULT?

    fun GetPropertyDescriptionList(
        keyType: PROPERTYKEY?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?,
    ): WinNT.HRESULT?

    fun GetAttributes(
        AttribFlags: Int,
        sfgaoMask: Int,
        psfgaoAttribs: IntByReference?,
    ): WinNT.HRESULT? // SIATTRIBFLAGS, SFGAOF,

    // SFGAOF
    fun GetCount(pdwNumItems: IntByReference?): WinNT.HRESULT?

    fun GetItemAt(dwIndex: Int, ppsi: PointerByReference?): WinNT.HRESULT? // IShellItem

    companion object {
        val IID_ISHELLITEMARRAY: GuidFixed.IID = GuidFixed.IID("{b63ea76d-1f85-456f-a19c-48159efa858b}") // Guid.IID
    }
}
