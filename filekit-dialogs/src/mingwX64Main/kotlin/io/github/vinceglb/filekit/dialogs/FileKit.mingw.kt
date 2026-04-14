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
    CoInitializeEx(null, (COINIT_APARTMENTTHREADED or COINIT_DISABLE_OLE1DDE).toUInt())
    val ppDlg = alloc<ComPtrVar>()
    try {
        if (fk_create_open_dialog(ppDlg.ptr.reinterpret()) != S_OK) return@memScoped null
        val dlg = ppDlg.value ?: return@memScoped null

        // Options
        val optsVar = alloc<DWORDVar>()
        fk_dialog_get_options(dlg.reinterpret(), optsVar.ptr)
        var opts = optsVar.value.toInt() or FK_FOS_FORCEFILESYSTEM or FK_FOS_PATHMUSTEXIST
        if (pickFolders) opts = opts or FK_FOS_PICKFOLDERS else opts = opts or FK_FOS_FILEMUSTEXIST
        if (allowMultiple) opts = opts or FK_FOS_ALLOWMULTISELECT
        fk_dialog_set_options(dlg.reinterpret(), opts.toUInt())

        title?.let { fk_dialog_set_title(dlg.reinterpret(), it) }
        directory?.let { setFolder(dlg, it) }
        if (!extensions.isNullOrEmpty() && !pickFolders) setFileTypes(dlg, extensions)

        val hr = fk_dialog_show(dlg.reinterpret(), null)
        if (hr != S_OK) return@memScoped null

        if (allowMultiple) {
            getMultipleResults(dlg)
        } else {
            val sigdn = if (pickFolders) FK_SIGDN_DESKTOPABSOLUTEPARSING.toInt() else FK_SIGDN_FILESYSPATH.toInt()
            getSingleResult(dlg, sigdn)?.let { listOf(it) }
        }
    } finally {
        ppDlg.value?.let { fk_open_dialog_release(it.reinterpret()) }
        CoUninitialize()
    }
}

private fun showSaveDialog(
    suggestedName: String,
    extension: String?,
    directory: PlatformFile?,
    title: String?,
): PlatformFile? = memScoped {
    CoInitializeEx(null, (COINIT_APARTMENTTHREADED or COINIT_DISABLE_OLE1DDE).toUInt())
    val ppDlg = alloc<ComPtrVar>()
    try {
        if (fk_create_save_dialog(ppDlg.ptr.reinterpret()) != S_OK) return@memScoped null
        val dlg = ppDlg.value ?: return@memScoped null

        val optsVar = alloc<DWORDVar>()
        fk_dialog_get_options(dlg.reinterpret(), optsVar.ptr)
        val opts = optsVar.value.toInt() or FK_FOS_FORCEFILESYSTEM or FK_FOS_PATHMUSTEXIST or FK_FOS_OVERWRITEPROMPT
        fk_dialog_set_options(dlg.reinterpret(), opts.toUInt())

        title?.let { fk_dialog_set_title(dlg.reinterpret(), it) }
        fk_dialog_set_filename(dlg.reinterpret(), suggestedName)
        extension?.let {
            fk_dialog_set_default_extension(dlg.reinterpret(), it)
            setFileTypes(dlg, setOf(it))
        }
        directory?.let { setFolder(dlg, it) }

        val hr = fk_dialog_show(dlg.reinterpret(), null)
        if (hr != S_OK) return@memScoped null
        getSingleResult(dlg, FK_SIGDN_FILESYSPATH.toInt())
    } finally {
        ppDlg.value?.let { fk_save_dialog_release(it.reinterpret()) }
        CoUninitialize()
    }
}

// region Helpers

private fun MemScope.setFolder(dlg: ComPtr, dir: PlatformFile) {
    val ppsi = alloc<ComPtrVar>()
    if (fk_create_shell_item_from_path(dir.path, ppsi.ptr.reinterpret()) != S_OK) return
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
    fk_dialog_set_file_types(dlg.reinterpret(), 1u, spec.reinterpret())
}

private fun MemScope.getSingleResult(dlg: ComPtr, sigdn: Int): PlatformFile? {
    val ppsi = alloc<ComPtrVar>()
    if (fk_dialog_get_result(dlg.reinterpret(), ppsi.ptr.reinterpret()) != S_OK) return null
    val item = ppsi.value ?: return null
    try {
        return shellItemToFile(item, sigdn)
    } finally {
        fk_shell_item_release(item.reinterpret())
    }
}

private fun MemScope.getMultipleResults(dlg: ComPtr): List<PlatformFile> {
    val ppArr = alloc<ComPtrVar>()
    if (fk_open_dialog_get_results(dlg.reinterpret(), ppArr.ptr.reinterpret()) != S_OK) return emptyList()
    val arr = ppArr.value ?: return emptyList()
    try {
        val cntVar = alloc<DWORDVar>()
        fk_shell_item_array_get_count(arr.reinterpret(), cntVar.ptr)
        return (0 until cntVar.value.toInt()).mapNotNull { i ->
            val ppsi = alloc<ComPtrVar>()
            if (fk_shell_item_array_get_item_at(arr.reinterpret(), i.toUInt(), ppsi.ptr.reinterpret()) != S_OK) return@mapNotNull null
            val item = ppsi.value ?: return@mapNotNull null
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
    if (fk_shell_item_get_display_name(item.reinterpret(), sigdn, ppName.ptr.reinterpret()) != S_OK) return null
    val namePtr = ppName.value ?: return null
    try {
        return PlatformFile(namePtr.toKStringFromUtf16())
    } finally {
        CoTaskMemFree(namePtr)
    }
}

// endregion
