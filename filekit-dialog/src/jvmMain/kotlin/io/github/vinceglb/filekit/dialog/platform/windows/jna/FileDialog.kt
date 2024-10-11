package io.github.vinceglb.filekit.dialog.platform.windows.jna

import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.dialog.platform.windows.jna.ShTypes.COMDLG_FILTERSPEC

internal open class FileDialog : ModalWindow, IFileDialog {
    constructor()

    constructor(pvInstance: Pointer?) : super(pvInstance)

    // VTBL Id indexing starts at 4 after ModalWindow's 3
    override fun SetFileTypes(
        FileTypes: Int,
        rgFilterSpec: Array<COMDLG_FILTERSPEC?>?
    ): WinNT.HRESULT {
        return _invokeNativeObject(
            4, arrayOf(this.pointer, FileTypes, rgFilterSpec),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetFileTypeIndex(iFileType: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            5, arrayOf(this.pointer, iFileType),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetFileTypeIndex(piFileType: IntByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            6, arrayOf(this.pointer, piFileType),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun Advise(pfde: Pointer?, pdwCookie: IntByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            7, arrayOf(this.pointer, pfde, pdwCookie),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun Unadvise(dwCookie: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            8, arrayOf(this.pointer, dwCookie),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetOptions(fos: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            9,
            arrayOf(this.pointer, fos),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetOptions(pfos: IntByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            10, arrayOf(this.pointer, pfos),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetDefaultFolder(psi: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            11, arrayOf<Any?>(this.pointer, psi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetFolder(psi: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            12, arrayOf<Any?>(this.pointer, psi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetFolder(ppsi: PointerByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            13, arrayOf(this.pointer, ppsi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetCurrentSelection(ppsi: PointerByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            14, arrayOf(this.pointer, ppsi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetFileName(pszName: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            15, arrayOf(this.pointer, pszName),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetFileName(pszName: PointerByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            16, arrayOf(this.pointer, pszName),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetTitle(pszTitle: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            17, arrayOf(this.pointer, pszTitle),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetOkButtonLabel(pszText: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            18, arrayOf(this.pointer, pszText),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetFileNameLabel(pszLabel: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            19, arrayOf(this.pointer, pszLabel),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun GetResult(ppsi: PointerByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            20, arrayOf(this.pointer, ppsi),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun AddPlace(psi: Pointer?, fdap: Int): WinNT.HRESULT {
        return _invokeNativeObject(
            21, arrayOf(this.pointer, psi, fdap),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetDefaultExtension(pszDefaultExtension: WString?): WinNT.HRESULT {
        return _invokeNativeObject(
            22, arrayOf(this.pointer, pszDefaultExtension),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun Close(hr: WinNT.HRESULT?): WinNT.HRESULT {
        return _invokeNativeObject(
            23, arrayOf(this.pointer, hr),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetClientGuid(guid: Guid.GUID.ByReference?): WinNT.HRESULT {
        return _invokeNativeObject(
            24, arrayOf(this.pointer, guid),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun ClearClientData(): WinNT.HRESULT {
        return _invokeNativeObject(
            25, arrayOf<Any>(this.pointer),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }

    override fun SetFilter(pFilter: Pointer?): WinNT.HRESULT {
        return _invokeNativeObject(
            26, arrayOf<Any?>(this.pointer, pFilter),
            WinNT.HRESULT::class.java
        ) as WinNT.HRESULT
    }
}
