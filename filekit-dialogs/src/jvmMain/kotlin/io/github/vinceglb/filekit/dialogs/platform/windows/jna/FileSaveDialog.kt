package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.PointerByReference

internal class FileSaveDialog :
    FileDialog,
    IFileSaveDialog {
    constructor()

    constructor(pvInstance: Pointer?) : super(pvInstance)

    // VTBL Id indexing starts at 27 after IFileDialog's 26
    override fun SetSaveAsItem(psi: Pointer?): WinNT.HRESULT = _invokeNativeObject(
        27,
        arrayOf<Any?>(this.pointer, psi),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun SetProperties(pStore: Pointer?): WinNT.HRESULT = _invokeNativeObject(
        28,
        arrayOf<Any?>(this.pointer, pStore),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun SetCollectedProperties(
        pList: Pointer?,
        fAppendDefault: Boolean,
    ): WinNT.HRESULT = _invokeNativeObject(
        29,
        arrayOf(this.pointer, pList, fAppendDefault),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun GetProperties(ppStore: PointerByReference?): WinNT.HRESULT = _invokeNativeObject(
        30,
        arrayOf(this.pointer, ppStore),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT

    override fun ApplyProperties(
        psi: Pointer?,
        pStore: Pointer?,
        hwnd: WinDef.HWND?,
        pSink: Pointer?,
    ): WinNT.HRESULT = _invokeNativeObject(
        31,
        arrayOf(this.pointer, psi, pStore, hwnd, pSink),
        WinNT.HRESULT::class.java,
    ) as WinNT.HRESULT
}
