package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.GETPROPERTYSTOREFLAGS
import io.github.vinceglb.filekit.dialogs.platform.windows.jna.ShTypes.PROPERTYKEY

internal class ShellItemArray : Unknown, IShellItemArray {
    constructor()

    constructor(pvInstance: Pointer?) : super(pvInstance)

    // VTBL Id indexing starts at 3 after Unknown's 0, 1, 2
    override fun BindToHandler(
        pbc: Pointer?,
        bhid: Guid.GUID.ByReference?,
        riid: Guid.REFIID?,
        ppvOut: PointerByReference?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            3, arrayOf(this.pointer, pbc, bhid, riid, ppvOut),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetPropertyStore(
        flags: GETPROPERTYSTOREFLAGS?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            4, arrayOf(this.pointer, flags, riid, ppv),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetPropertyDescriptionList(
        keyType: PROPERTYKEY?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            5, arrayOf(this.pointer, keyType, riid, ppv),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetAttributes(
        AttribFlags: Int,
        sfgaoMask: Int,
        psfgaoAttribs: IntByReference?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            6,
            arrayOf(this.pointer, AttribFlags, sfgaoMask, psfgaoAttribs),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetCount(pdwNumItems: IntByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            7, arrayOf(this.pointer, pdwNumItems),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetItemAt(dwIndex: Int, ppsi: PointerByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            8, arrayOf(this.pointer, dwIndex, ppsi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }
}