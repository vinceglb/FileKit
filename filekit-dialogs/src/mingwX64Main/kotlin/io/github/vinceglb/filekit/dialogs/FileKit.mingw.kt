@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.dialogs

import comdialogs.FK_FOS_ALLOWMULTISELECT
import comdialogs.FK_FOS_FILEMUSTEXIST
import comdialogs.FK_FOS_FORCEFILESYSTEM
import comdialogs.FK_FOS_OVERWRITEPROMPT
import comdialogs.FK_FOS_PATHMUSTEXIST
import comdialogs.FK_FOS_PICKFOLDERS
import comdialogs.FK_SIGDN_DESKTOPABSOLUTEPARSING
import comdialogs.FK_SIGDN_FILESYSPATH
import comdialogs.fk_create_open_dialog
import comdialogs.fk_create_save_dialog
import comdialogs.fk_create_shell_item_from_path
import comdialogs.fk_dialog_get_options
import comdialogs.fk_dialog_get_result
import comdialogs.fk_dialog_set_default_extension
import comdialogs.fk_dialog_set_file_types
import comdialogs.fk_dialog_set_filename
import comdialogs.fk_dialog_set_folder
import comdialogs.fk_dialog_set_options
import comdialogs.fk_dialog_set_title
import comdialogs.fk_dialog_show
import comdialogs.fk_open_dialog_get_results
import comdialogs.fk_open_dialog_release
import comdialogs.fk_save_dialog_release
import comdialogs.fk_shell_item_array_get_count
import comdialogs.fk_shell_item_array_get_item_at
import comdialogs.fk_shell_item_array_release
import comdialogs.fk_shell_item_get_display_name
import comdialogs.fk_shell_item_release
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import kotlinx.cinterop.wcstr
import kotlinx.coroutines.flow.Flow
import platform.windows.COINIT_APARTMENTTHREADED
import platform.windows.COINIT_DISABLE_OLE1DDE
import platform.windows.CoInitializeEx
import platform.windows.CoTaskMemFree
import platform.windows.CoUninitialize
import platform.windows.DWORDVar
import platform.windows.S_OK
import platform.windows.ShellExecuteW

// Opaque pointer type for COM objects (cinterop doesn't export C++ interfaces)
private typealias ComPtr = CPointer<ByteVar>
private typealias ComPtrVar = CPointerVar<ByteVar>

private const val S_FALSE_HRESULT = 1
private val ERROR_CANCELLED_HRESULT = 0x800704C7u.toInt()
private val ERROR_FILE_NOT_FOUND_HRESULT = 0x80070002u.toInt()
private val ERROR_INVALID_DRIVE_HRESULT = 0x8007000Fu.toInt()

internal actual suspend fun FileKit.platformOpenFilePicker(
    type: FileKitType,
    mode: PickerMode,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): Flow<FileKitPickerState<List<PlatformFile>>> {
    val extensions = when (type) {
        FileKitType.Image -> imageExtensions
        FileKitType.Video -> videoExtensions
        FileKitType.ImageAndVideo -> imageExtensions + videoExtensions
        is FileKitType.File -> type.extensions
    }
    return showOpenDialog(extensions, directory, dialogSettings.title, pickFolders = false, mode is PickerMode.Multiple)
        .toPickerStateFlow()
}

public actual suspend fun FileKit.openDirectoryPicker(
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? =
    showOpenDialog(null, directory, dialogSettings.title, pickFolders = true, allowMultiple = false)
        ?.firstOrNull()

public actual suspend fun FileKit.openFileSaver(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    dialogSettings: FileKitDialogSettings,
): PlatformFile? {
    val ext = normalizeFileSaverExtension(extension)
    return showSaveDialog(buildFileSaverSuggestedName(suggestedName, ext), ext, directory, dialogSettings.title)
}

public actual fun FileKit.openFileWithDefaultApplication(
    file: PlatformFile,
    openFileSettings: FileKitOpenFileSettings,
) {
    ShellExecuteW(null, "open", file.path, null, null, 1)
}

private fun showOpenDialog(
    extensions: Set<String>?,
    directory: PlatformFile?,
    title: String?,
    pickFolders: Boolean,
    allowMultiple: Boolean,
): List<PlatformFile>? = memScoped {
    val comInitialized = initializeComForDialogs()
    val ppDlg = alloc<ComPtrVar>()
    try {
        val createHr = fk_create_open_dialog(ppDlg.ptr.reinterpret())
        if (createHr != S_OK) {
            throw IllegalStateException("CoCreateInstance(IFileOpenDialog) failed with HRESULT 0x${createHr.toUInt().toString(16)}")
        }
        val dlg = ppDlg.value
            ?: throw IllegalStateException("CoCreateInstance(IFileOpenDialog) returned a null dialog pointer")

        // Options
        val optsVar = alloc<DWORDVar>()
        val getOptionsHr = fk_dialog_get_options(dlg.reinterpret(), optsVar.ptr)
        if (getOptionsHr != S_OK) {
            throw IllegalStateException("IFileDialog::GetOptions failed with HRESULT 0x${getOptionsHr.toUInt().toString(16)}")
        }
        var opts = optsVar.value.toInt() or FK_FOS_FORCEFILESYSTEM or FK_FOS_PATHMUSTEXIST
        if (pickFolders) opts = opts or FK_FOS_PICKFOLDERS else opts = opts or FK_FOS_FILEMUSTEXIST
        if (allowMultiple) opts = opts or FK_FOS_ALLOWMULTISELECT
        val setOptionsHr = fk_dialog_set_options(dlg.reinterpret(), opts.toUInt())
        if (setOptionsHr != S_OK) {
            throw IllegalStateException("IFileDialog::SetOptions failed with HRESULT 0x${setOptionsHr.toUInt().toString(16)}")
        }

        title?.let {
            val setTitleHr = fk_dialog_set_title(dlg.reinterpret(), it)
            if (setTitleHr != S_OK) {
                throw IllegalStateException("IFileDialog::SetTitle failed with HRESULT 0x${setTitleHr.toUInt().toString(16)}")
            }
        }
        directory?.let { setFolder(dlg, it) }
        if (!extensions.isNullOrEmpty() && !pickFolders) setFileTypes(dlg, extensions)

        val hr = fk_dialog_show(dlg.reinterpret(), null)
        if (hr != S_OK) {
            if (hr == ERROR_CANCELLED_HRESULT) {
                return@memScoped null
            }
            throw IllegalStateException("IFileOpenDialog::Show failed with HRESULT 0x${hr.toUInt().toString(16)}")
        }

        if (allowMultiple) {
            getMultipleResults(dlg)
        } else {
            val sigdn = if (pickFolders) FK_SIGDN_DESKTOPABSOLUTEPARSING.toInt() else FK_SIGDN_FILESYSPATH.toInt()
            getSingleResult(dlg, sigdn)?.let { listOf(it) }
        }
    } finally {
        ppDlg.value?.let { fk_open_dialog_release(it.reinterpret()) }
        if (comInitialized) {
            CoUninitialize()
        }
    }
}

private fun showSaveDialog(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    title: String?,
): PlatformFile? = memScoped {
    val comInitialized = initializeComForDialogs()
    val ppDlg = alloc<ComPtrVar>()
    try {
        val createHr = fk_create_save_dialog(ppDlg.ptr.reinterpret())
        if (createHr != S_OK) {
            throw IllegalStateException("CoCreateInstance(IFileSaveDialog) failed with HRESULT 0x${createHr.toUInt().toString(16)}")
        }
        val dlg = ppDlg.value
            ?: throw IllegalStateException("CoCreateInstance(IFileSaveDialog) returned a null dialog pointer")

        val optsVar = alloc<DWORDVar>()
        val getOptionsHr = fk_dialog_get_options(dlg.reinterpret(), optsVar.ptr)
        if (getOptionsHr != S_OK) {
            throw IllegalStateException("IFileDialog::GetOptions failed with HRESULT 0x${getOptionsHr.toUInt().toString(16)}")
        }
        val opts = optsVar.value.toInt() or FK_FOS_FORCEFILESYSTEM or FK_FOS_PATHMUSTEXIST or FK_FOS_OVERWRITEPROMPT
        val setOptionsHr = fk_dialog_set_options(dlg.reinterpret(), opts.toUInt())
        if (setOptionsHr != S_OK) {
            throw IllegalStateException("IFileDialog::SetOptions failed with HRESULT 0x${setOptionsHr.toUInt().toString(16)}")
        }

        title?.let {
            val setTitleHr = fk_dialog_set_title(dlg.reinterpret(), it)
            if (setTitleHr != S_OK) {
                throw IllegalStateException("IFileDialog::SetTitle failed with HRESULT 0x${setTitleHr.toUInt().toString(16)}")
            }
        }
        val setFilenameHr = fk_dialog_set_filename(dlg.reinterpret(), suggestedName)
        if (setFilenameHr != S_OK) {
            throw IllegalStateException("IFileDialog::SetFileName failed with HRESULT 0x${setFilenameHr.toUInt().toString(16)}")
        }
        extension?.let {
            val setDefaultExtensionHr = fk_dialog_set_default_extension(dlg.reinterpret(), it)
            if (setDefaultExtensionHr != S_OK) {
                throw IllegalStateException(
                    "IFileDialog::SetDefaultExtension failed with HRESULT 0x${setDefaultExtensionHr.toUInt().toString(16)}",
                )
            }
            setFileTypes(dlg, setOf(it))
        }
        directory?.let { setFolder(dlg, it) }

        val hr = fk_dialog_show(dlg.reinterpret(), null)
        if (hr != S_OK) {
            if (hr == ERROR_CANCELLED_HRESULT) {
                return@memScoped null
            }
            throw IllegalStateException("IFileSaveDialog::Show failed with HRESULT 0x${hr.toUInt().toString(16)}")
        }
        getSingleResult(dlg, FK_SIGDN_FILESYSPATH.toInt())
    } finally {
        ppDlg.value?.let { fk_save_dialog_release(it.reinterpret()) }
        if (comInitialized) {
            CoUninitialize()
        }
    }
}

// region Helpers

private fun initializeComForDialogs(): Boolean {
    val result = CoInitializeEx(
        null,
        COINIT_APARTMENTTHREADED or COINIT_DISABLE_OLE1DDE,
    )

    if (result == S_OK || result == S_FALSE_HRESULT) {
        return true
    }

    throw IllegalStateException("CoInitializeEx failed with HRESULT 0x${result.toUInt().toString(16)}")
}

private fun MemScope.setFolder(dlg: ComPtr, dir: PlatformFile) {
    val ppsi = alloc<ComPtrVar>()
    val hr = fk_create_shell_item_from_path(dir.path, ppsi.ptr.reinterpret())
    if (hr != S_OK) {
        if (hr == ERROR_FILE_NOT_FOUND_HRESULT || hr == ERROR_INVALID_DRIVE_HRESULT) {
            return
        }
        throw IllegalStateException("SHCreateItemFromParsingName failed with HRESULT 0x${hr.toUInt().toString(16)}")
    }
    val folder = ppsi.value ?: return
    try {
        fk_dialog_set_folder(dlg.reinterpret(), folder.reinterpret())
    } finally {
        fk_shell_item_release(folder.reinterpret())
    }
}

private fun MemScope.setFileTypes(dlg: ComPtr, exts: Set<String>) {
    val display = exts.joinToString(", ") { "*.$it" }
    val pattern = exts.joinToString(";") { "*.$it" }
    // COMDLG_FILTERSPEC = { LPCWSTR pszName; LPCWSTR pszSpec; } = two consecutive pointers
    val spec = allocArray<CPointerVar<UShortVar>>(2)
    spec[0] = display.wcstr.ptr
    spec[1] = pattern.wcstr.ptr
    val hr = fk_dialog_set_file_types(dlg.reinterpret(), 1u, spec.reinterpret())
    if (hr != S_OK) {
        throw IllegalStateException("IFileDialog::SetFileTypes failed with HRESULT 0x${hr.toUInt().toString(16)}")
    }
}

private fun MemScope.getSingleResult(dlg: ComPtr, sigdn: Int): PlatformFile? {
    val ppsi = alloc<ComPtrVar>()
    val hr = fk_dialog_get_result(dlg.reinterpret(), ppsi.ptr.reinterpret())
    if (hr != S_OK) {
        throw IllegalStateException("IFileDialog::GetResult failed with HRESULT 0x${hr.toUInt().toString(16)}")
    }
    val item = ppsi.value
        ?: throw IllegalStateException("IFileDialog::GetResult returned a null result item")
    try {
        return shellItemToFile(item, sigdn)
    } finally {
        fk_shell_item_release(item.reinterpret())
    }
}

private fun MemScope.getMultipleResults(dlg: ComPtr): List<PlatformFile> {
    val ppArr = alloc<ComPtrVar>()
    val resultsHr = fk_open_dialog_get_results(dlg.reinterpret(), ppArr.ptr.reinterpret())
    if (resultsHr != S_OK) {
        throw IllegalStateException("IFileOpenDialog::GetResults failed with HRESULT 0x${resultsHr.toUInt().toString(16)}")
    }
    val arr = ppArr.value
        ?: throw IllegalStateException("IFileOpenDialog::GetResults returned a null result array")
    try {
        val cntVar = alloc<DWORDVar>()
        val countHr = fk_shell_item_array_get_count(arr.reinterpret(), cntVar.ptr)
        if (countHr != S_OK) {
            throw IllegalStateException("IShellItemArray::GetCount failed with HRESULT 0x${countHr.toUInt().toString(16)}")
        }
        return (0 until cntVar.value.toInt()).mapNotNull { i ->
            val ppsi = alloc<ComPtrVar>()
            val itemHr = fk_shell_item_array_get_item_at(arr.reinterpret(), i.toUInt(), ppsi.ptr.reinterpret())
            if (itemHr != S_OK) {
                throw IllegalStateException("IShellItemArray::GetItemAt failed with HRESULT 0x${itemHr.toUInt().toString(16)}")
            }
            val item = ppsi.value
                ?: throw IllegalStateException("IShellItemArray::GetItemAt returned a null shell item")
            try {
                shellItemToFile(item, FK_SIGDN_FILESYSPATH.toInt())
            } finally {
                fk_shell_item_release(item.reinterpret())
            }
        }
    } finally {
        fk_shell_item_array_release(arr.reinterpret())
    }
}

private fun MemScope.shellItemToFile(item: ComPtr, sigdn: Int): PlatformFile? {
    val ppName = alloc<CPointerVar<UShortVar>>()
    val hr = fk_shell_item_get_display_name(item.reinterpret(), sigdn, ppName.ptr.reinterpret())
    if (hr != S_OK) {
        throw IllegalStateException("IShellItem::GetDisplayName failed with HRESULT 0x${hr.toUInt().toString(16)}")
    }
    val namePtr = ppName.value
        ?: throw IllegalStateException("IShellItem::GetDisplayName returned a null display name")
    try {
        return PlatformFile(namePtr.toKStringFromUtf16())
    } finally {
        CoTaskMemFree(namePtr)
    }
}

// endregion
