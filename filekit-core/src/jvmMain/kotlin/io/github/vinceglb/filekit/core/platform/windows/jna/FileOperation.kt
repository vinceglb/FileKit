package io.github.vinceglb.filekit.core.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference


internal class FileOperation : Unknown, IFileOperation {
    constructor()

    constructor(pvInstance: Pointer?) : super(pvInstance)

    // VTBL Id indexing starts at 3 after Unknown's 2
    override fun Advise(pfops: Pointer?, pdwCookie: IntByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            3, arrayOf(this.pointer, pfops, pdwCookie),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun Unadvise(dwCookie: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            4, arrayOf(this.pointer, dwCookie),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetOperationFlags(dwOperationFlags: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            5, arrayOf(this.pointer, dwOperationFlags),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetProgressMessage(pszMessage: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            6, arrayOf(this.pointer, pszMessage),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetProgressDialog(popd: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            7, arrayOf<Any?>(this.pointer, popd),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetProperties(pproparray: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            8, arrayOf<Any?>(this.pointer, pproparray),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetOwnerWindow(hwndOwner: WinDef.HWND?): WinNT.HRESULT {
        return _invokeNativeObject(
            9, arrayOf(this.pointer, hwndOwner),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun ApplyPropertiesToItem(psiItem: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            10, arrayOf<Any?>(this.pointer, psiItem),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun ApplyPropertiesToItems(punkItems: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            11, arrayOf<Any?>(this.pointer, punkItems),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun RenameItem(
        psiItem: Pointer?,
        pszNewName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            12,
            arrayOf(this.pointer, psiItem, pszNewName, pfopsItem),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun RenameItems(pUnkItems: Pointer?, pszNewName: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            13, arrayOf(this.pointer, pUnkItems, pszNewName),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun MoveItem(
        psiItem: Pointer?,
        psiDestinationFolder: Pointer?,
        pszNewName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            14,
            arrayOf(this.pointer, psiItem, psiDestinationFolder, pszNewName, pfopsItem),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun MoveItems(
        punkItems: Pointer?,
        psiDestinationFolder: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            15,
            arrayOf<Any?>(this.pointer, punkItems, psiDestinationFolder),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun CopyItem(
        psiItem: Pointer?,
        psiDestinationFolder: Pointer?,
        pszCopyName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            16,
            arrayOf(this.pointer, psiItem, psiDestinationFolder, pszCopyName, pfopsItem),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun CopyItems(
        punkItems: Pointer?,
        psiDestinationFolder: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            17,
            arrayOf<Any?>(this.pointer, punkItems, psiDestinationFolder),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun DeleteItem(psiItem: Pointer?, pfopsItem: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            18, arrayOf<Any?>(this.pointer, psiItem, pfopsItem),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun DeleteItems(punkItems: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            19, arrayOf<Any?>(this.pointer, punkItems),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun NewItem(
        psiDestinationFolder: Pointer?,
        dwFileAttributes: Int,
        pszName: WString?,
        pszTemplateName: WString?,
        pfopsItem: Pointer?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            20, arrayOf(
                this.pointer, psiDestinationFolder,
                dwFileAttributes, pszName, pszTemplateName, pfopsItem
            ),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun PerformOperations(): WinNT.HRESULT {
        return _invokeNativeObject(
            21, arrayOf<Any>(this.pointer),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetAnyOperationsAborted(pfAnyOperationsAborted: WinDef.BOOLByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            22, arrayOf(this.pointer, pfAnyOperationsAborted),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }
}
