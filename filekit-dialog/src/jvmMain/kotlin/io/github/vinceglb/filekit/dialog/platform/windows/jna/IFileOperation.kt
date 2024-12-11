package io.github.vinceglb.filekit.dialog.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.IUnknown
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import io.github.vinceglb.filekit.dialog.platform.windows.util.GuidFixed


internal interface IFileOperation : IUnknown {
    fun Advise(
        pfops: Pointer?,
        pdwCookie: IntByReference?
    ): WinNT.HRESULT? // IFileOperationProgressSink

    fun Unadvise(dwCookie: Int): WinNT.HRESULT?

    fun SetOperationFlags(dwOperationFlags: Int): WinNT.HRESULT?

    fun SetProgressMessage(pszMessage: WString?): WinNT.HRESULT?

    fun SetProgressDialog(popd: Pointer?): WinNT.HRESULT? // IOperationsProgressDialog

    fun SetProperties(pproparray: Pointer?): WinNT.HRESULT? // IPropertyChangeArray

    fun SetOwnerWindow(hwndOwner: WinDef.HWND?): WinNT.HRESULT?

    fun ApplyPropertiesToItem(psiItem: Pointer?): WinNT.HRESULT? // IShellItem

    fun ApplyPropertiesToItems(punkItems: Pointer?): WinNT.HRESULT? // IUnknown

    fun RenameItem(
        psiItem: Pointer?,
        pszNewName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT? // IShellItem,

    // IFileOperationProgressSink
    fun RenameItems(pUnkItems: Pointer?, pszNewName: WString?): WinNT.HRESULT? // IUnknown

    fun MoveItem(
        psiItem: Pointer?,
        psiDestinationFolder: Pointer?,
        pszNewName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT? // IShellItem,

    // IShellItem,
    // IFileOperationProgressSink
    fun MoveItems(
        punkItems: Pointer?,
        psiDestinationFolder: Pointer?
    ): WinNT.HRESULT? // IUnknown, IShellItem

    fun CopyItem(
        psiItem: Pointer?,
        psiDestinationFolder: Pointer?,
        pszCopyName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT? // IShellItem,

    // IShellItem,
    // IFileOperationProgressSink
    fun CopyItems(
        punkItems: Pointer?,
        psiDestinationFolder: Pointer?
    ): WinNT.HRESULT? // IUnknown, IShellItem

    fun DeleteItem(
        psiItem: Pointer?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT? // IShellItem, IFileOperationProgressSink

    fun DeleteItems(punkItems: Pointer?): WinNT.HRESULT? // IUnknown

    fun NewItem(
        psiDestinationFolder: Pointer?,
        dwFileAttributes: Int,
        pszName: WString?,
        pszTemplateName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT? // IShellItem, IFileOperationProgressSink

    fun PerformOperations(): WinNT.HRESULT?

    fun GetAnyOperationsAborted(pfAnyOperationsAborted: WinDef.BOOLByReference?): WinNT.HRESULT?

    companion object {
        val IID_IFILEOPERATION: GuidFixed.IID = GuidFixed.IID("{947aab5f-0a5c-4c13-b4d6-4bf7836fc9f8}") // Guid.IID
        val CLSID_FILEOPERATION: GuidFixed.CLSID = GuidFixed.CLSID("{3ad05575-8857-4850-9277-11b85bdb8e09}") // Guid.CLSID
    }
}
