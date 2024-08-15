package io.github.vinceglb.filekit.core.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.core.platform.windows.util.GuidFixed


internal interface IFileSaveDialog : IFileDialog {
    fun SetSaveAsItem(psi: Pointer?): WinNT.HRESULT?

    fun SetProperties(pStore: Pointer?): WinNT.HRESULT? // IPropertyStore

    fun SetCollectedProperties(
        pList: Pointer?,
        fAppendDefault: Boolean
    ): WinNT.HRESULT? // IPropertyDescriptionList

    fun GetProperties(ppStore: PointerByReference?): WinNT.HRESULT? // IPropertyStore

    fun ApplyProperties(
        psi: Pointer?,
        pStore: Pointer?,
        hwnd: WinDef.HWND?,
        pSink: Pointer?
    ): WinNT.HRESULT? // IShellItem, IPropertyStore, HWND, IFileOperationProgressSink

    companion object {
        val IID_IFILESAVEDIALOG: GuidFixed.IID = GuidFixed.IID("{84bccd23-5fde-4cdb-aea4-af64b83d78ab}")
        val CLSID_FILESAVEDIALOG: GuidFixed.CLSID = GuidFixed.CLSID("{C0B4E2F3-BA21-4773-8DBA-335EC946EB8B}")
    }
}
