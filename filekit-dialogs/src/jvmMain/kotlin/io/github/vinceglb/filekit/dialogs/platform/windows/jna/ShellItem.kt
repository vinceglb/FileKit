package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

internal class ShellItem :
    Unknown,
    IShellItem {
    constructor()

    constructor(pvInstance: Pointer?) : super(pvInstance)

    // VTBL Id indexing starts at 3 after Unknown's 0, 1, 2
    override fun BindToHandler(
        pbc: Pointer?,
        bhid: Guid.GUID.ByReference?,
        riid: Guid.REFIID?,
        ppv: PointerByReference?,
    ): WinNT.HRESULT = _invokeNativeObject(
        3,
        arrayOf(this.pointer, pbc, bhid, riid, ppv),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun GetParent(ppsi: PointerByReference?): WinNT.HRESULT = _invokeNativeObject(
        4,
        arrayOf(this.pointer, ppsi),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun GetDisplayName(
        sigdnName: Long,
        ppszName: PointerByReference?,
    ): WinNT.HRESULT = _invokeNativeObject(
        5,
        arrayOf(this.pointer, sigdnName, ppszName),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun GetAttributes(
        sfgaoMask: Int,
        psfgaoAttribs: IntByReference?,
    ): WinNT.HRESULT = _invokeNativeObject(
        6,
        arrayOf(this.pointer, sfgaoMask, psfgaoAttribs),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun Compare(
        psi: Pointer?,
        hint: Int,
        piOrder: IntByReference?,
    ): WinNT.HRESULT = _invokeNativeObject(
        7,
        arrayOf(this.pointer, psi, hint, piOrder),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT
}
