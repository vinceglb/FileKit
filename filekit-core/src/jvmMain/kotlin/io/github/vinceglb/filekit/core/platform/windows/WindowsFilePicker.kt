package io.github.vinceglb.filekit.core.platform.windows

import com.sun.jna.Native
import com.sun.jna.WString
import com.sun.jna.platform.win32.COM.COMUtils
import com.sun.jna.platform.win32.COM.COMUtils.FAILED
import com.sun.jna.platform.win32.Guid
import com.sun.jna.platform.win32.Ole32
import com.sun.jna.platform.win32.Ole32.COINIT_APARTMENTTHREADED
import com.sun.jna.platform.win32.WTypes
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinError.ERROR_CANCELLED
import com.sun.jna.platform.win32.WinError.ERROR_FILE_NOT_FOUND
import com.sun.jna.platform.win32.WinError.ERROR_INVALID_DRIVE
import com.sun.jna.platform.win32.WinNT.HRESULT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.platform.PlatformFilePicker
import io.github.vinceglb.filekit.core.platform.windows.jna.FileDialog
import io.github.vinceglb.filekit.core.platform.windows.jna.FileOpenDialog
import io.github.vinceglb.filekit.core.platform.windows.jna.FileSaveDialog
import io.github.vinceglb.filekit.core.platform.windows.jna.IFileOpenDialog
import io.github.vinceglb.filekit.core.platform.windows.jna.IFileSaveDialog
import io.github.vinceglb.filekit.core.platform.windows.jna.IShellItem
import io.github.vinceglb.filekit.core.platform.windows.jna.ShTypes.COMDLG_FILTERSPEC
import io.github.vinceglb.filekit.core.platform.windows.jna.ShTypes.FILEOPENDIALOGOPTIONS.Companion.FOS_ALLOWMULTISELECT
import io.github.vinceglb.filekit.core.platform.windows.jna.ShTypes.FILEOPENDIALOGOPTIONS.Companion.FOS_PICKFOLDERS
import io.github.vinceglb.filekit.core.platform.windows.jna.ShTypes.SIGDN.Companion.SIGDN_DESKTOPABSOLUTEPARSING
import io.github.vinceglb.filekit.core.platform.windows.jna.ShTypes.SIGDN.Companion.SIGDN_FILESYSPATH
import io.github.vinceglb.filekit.core.platform.windows.jna.Shell32
import io.github.vinceglb.filekit.core.platform.windows.jna.ShellItem
import io.github.vinceglb.filekit.core.platform.windows.jna.ShellItemArray
import io.github.vinceglb.filekit.core.platform.windows.util.GuidFixed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Window
import java.io.File

internal class WindowsFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitPlatformSettings?,
    ): File? = useFileDialog(FileDialogType.Open) { fileOpenDialog ->
        // Set the initial directory
        initialDirectory?.let { fileOpenDialog.setDefaultPath(it) }

        // Set title
        title?.let {
            fileOpenDialog
                .SetTitle(WString(title))
                .verify("SetTitle failed")
        }

        // Add filters
        fileExtensions
            ?.takeIf { it.isNotEmpty() }
            ?.let { fileOpenDialog.addFiltersToDialog(it) }

        fileOpenDialog.show(platformSettings?.parentWindow) {
            it.getResult(SIGDN_FILESYSPATH)
        }
    }

    override suspend fun pickFiles(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitPlatformSettings?,
    ): List<File>? = useFileDialog(FileDialogType.Open) { fileOpenDialog ->
        // Set the initial directory
        initialDirectory?.let { fileOpenDialog.setDefaultPath(it) }

        // Set title
        title?.let {
            fileOpenDialog
                .SetTitle(WString(title))
                .verify("SetTitle failed")
        }

        // Add filters
        fileExtensions
            ?.takeIf { it.isNotEmpty() }
            ?.let { fileOpenDialog.addFiltersToDialog(it) }

        // Set a flag for multiple options
        fileOpenDialog.setFlag(FOS_ALLOWMULTISELECT)

        fileOpenDialog.show(platformSettings?.parentWindow) {
            it.getResults()
        }
    }

    override suspend fun pickDirectory(
        initialDirectory: String?,
        title: String?,
        platformSettings: FileKitPlatformSettings?,
    ): File? = useFileDialog(FileDialogType.Open) { fileOpenDialog ->
        // Set the initial directory
        initialDirectory?.let { fileOpenDialog.setDefaultPath(it) }

        // Set title
        title?.let {
            fileOpenDialog
                .SetTitle(WString(title))
                .verify("SetTitle failed")
        }

        // Add in FOS_PICKFOLDERS which hides files and only allows selection of folders
        fileOpenDialog.setFlag(FOS_PICKFOLDERS)

        // Show the dialog to the user
        fileOpenDialog.show(platformSettings?.parentWindow) {
            it.getResult(SIGDN_DESKTOPABSOLUTEPARSING)
        }
    }

    override suspend fun saveFile(
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: FileKitPlatformSettings?,
    ): File? = useFileDialog(FileDialogType.Save) { fileSaveDialog ->
        // Set the initial directory
        initialDirectory?.let { fileSaveDialog.setDefaultPath(it) }

        // Set the default file name
        fileSaveDialog
            .SetFileName(WString(baseName))
            .verify("SetFileName failed")

        // Set the default extension
        fileSaveDialog
            .SetDefaultExtension(WString(extension))
            .verify("SetDefaultExtension failed")

        // Set filters
        fileSaveDialog.addFiltersToDialog(listOf(extension))

        // Show the dialog to the user
        fileSaveDialog.show(platformSettings?.parentWindow) {
            val file = it.getResult(SIGDN_FILESYSPATH)

            // Write the bytes to the file
            bytes?.let { bytes -> file.writeBytes(bytes) }

            file
        }
    }

    private suspend fun <FD : FileDialog, T> useFileDialog(
        type: FileDialogType<FD>,
        block: (FD) -> T
    ): T = withContext(Dispatchers.IO) {
        var fileDialog: FD? = null
        try {
            // Initialize COM
            initCom()

            // Create FileOpenDialog
            val pbrFileDialog = PointerByReference()
            fileDialog = Ole32.INSTANCE.CoCreateInstance(
                type.clsid,
                null,
                WTypes.CLSCTX_ALL,
                type.iid,
                pbrFileDialog
            )
                .verify("CoCreateInstance failed")
                .let { type.build(pbrFileDialog) }

            // Run the block
            block(fileDialog)
        } finally {
            fileDialog?.Release()
            Ole32.INSTANCE.CoUninitialize()
        }
    }

    sealed class FileDialogType<FD : FileDialog>(
        val clsid: GuidFixed.CLSID,
        val iid: GuidFixed.IID,
    ) {
        abstract fun build(pbr: PointerByReference): FD

        data object Open : FileDialogType<FileOpenDialog>(
            IFileOpenDialog.CLSID_FILEOPENDIALOG,
            IFileOpenDialog.IID_IFILEOPENDIALOG
        ) {
            override fun build(pbr: PointerByReference) = FileOpenDialog(pbr.value)
        }

        data object Save : FileDialogType<FileSaveDialog>(
            IFileSaveDialog.CLSID_FILESAVEDIALOG,
            IFileSaveDialog.IID_IFILESAVEDIALOG
        ) {
            override fun build(pbr: PointerByReference) = FileSaveDialog(pbr.value)
        }
    }

    private fun initCom() {
        Ole32.INSTANCE.CoInitializeEx(
            null,
            COINIT_APARTMENTTHREADED or Ole32.COINIT_DISABLE_OLE1DDE
        ).verify("CoInitializeEx failed")

        val isInit = COMUtils.comIsInitialized()
        if (!isInit) {
            throw RuntimeException("COM initialization failed")
        }
    }

    private fun FileDialog.setDefaultPath(defaultPath: String) {
        val pbrFolder = PointerByReference()
        val resultFolder = Shell32.INSTANCE.SHCreateItemFromParsingName(
            WString(defaultPath),
            null,
            Guid.REFIID(IShellItem.IID_ISHELLITEM),
            pbrFolder
        )

        // Valid error code: File not found
        val fileNotFoundException = Win32Exception(ERROR_FILE_NOT_FOUND)
        if (resultFolder == fileNotFoundException.hr) {
            println("FileKit - Initial directory not found: ${fileNotFoundException.message}")
            return
        }

        // Valid error code: Invalid drive
        val invalidDriveException = Win32Exception(ERROR_INVALID_DRIVE)
        if (resultFolder == invalidDriveException.hr) {
            println("FileKit - Invalid drive: ${invalidDriveException.message}")
            return
        }

        // Invalid error codes: throw exception
        if (FAILED(resultFolder)) {
            throw RuntimeException("SHCreateItemFromParsingName failed")
        }

        // Create ShellItem from the folder
        val folder = ShellItem(pbrFolder.value)

        // Set the initial directory
        this.SetFolder(folder.pointer)

        // Release the folder
        folder.Release()
    }

    private fun FileDialog.addFiltersToDialog(fileExtensions: List<String>) {
        // Create the filter string
        val filterString = fileExtensions.joinToString(";") { "*.$it" }

        val filterSpec = COMDLG_FILTERSPEC()
        filterSpec.pszName = WString("Files ($filterString)")
        filterSpec.pszSpec = WString(filterString)

        // Set the filter
        this.SetFileTypes(1, arrayOf(filterSpec))
    }

    private fun FileDialog.setFlag(flag: Int) {
        // Get the dialog options
        val ref = IntByReference()
        this.GetOptions(ref).verify("GetOptions failed")

        // Set the dialog options
        this.SetOptions(ref.value or flag).verify("SetOptions failed")
    }

    private fun <FD : FileDialog, T> FD.show(
        parentWindow: Window?,
        block: (FD) -> T
    ): T? {
        // Show the dialog to the user
        val openDialogResult = this.Show(parentWindow.toHwnd())

        // Valid error code: User canceled the dialog
        val userCanceledException = Win32Exception(ERROR_CANCELLED)
        if (openDialogResult == userCanceledException.hr) {
            return null
        }

        // Invalid error codes: throw exception
        if (FAILED(openDialogResult)) {
            throw RuntimeException("Show failed")
        }

        return block(this)
    }

    private fun FileDialog.getResult(sigdnName: Long): File {
        var item: ShellItem? = null
        var pbrDisplayName: PointerByReference? = null

        try {
            // Get the selected item
            val pbrItem = PointerByReference()
            this
                .GetResult(pbrItem)
                .verify("GetResult failed")

            // Create ShellItem from the pointer
            item = ShellItem(pbrItem.value)

            // Get the display name
            pbrDisplayName = PointerByReference()
            item
                .GetDisplayName(sigdnName, pbrDisplayName)
                .verify("GetDisplayName failed")

            // Get the path
            val path = pbrDisplayName.value.getWideString(0)

            // Return the file
            return File(path)
        } finally {
            // Release
            pbrDisplayName?.let { Ole32.INSTANCE.CoTaskMemFree(it.value) }
            item?.Release()
        }
    }

    private fun FileOpenDialog.getResults(): List<File> {
        var itemArray: ShellItemArray? = null

        try {
            // Get the selected item
            val pbrItemArray = PointerByReference()
            this
                .GetResults(pbrItemArray)
                .verify("GetResults failed")

            // Create ShellItemArray from the pointer
            itemArray = ShellItemArray(pbrItemArray.value)

            // Get the count
            val countRef = IntByReference()
            itemArray
                .GetCount(countRef)
                .verify("GetCount failed")

            // Get the items
            val files = mutableListOf<File>()
            for (i in 0 until countRef.value) {
                val pbrItem = PointerByReference()
                itemArray
                    .GetItemAt(i, pbrItem)
                    .verify("GetItemAt failed")

                // Create ShellItem from the pointer
                val item = ShellItem(pbrItem.value)

                // Get the display name
                val pbrDisplayName = PointerByReference()
                item
                    .GetDisplayName(SIGDN_FILESYSPATH, pbrDisplayName)
                    .verify("GetDisplayName failed")

                // Get the path
                val path = pbrDisplayName.value.getWideString(0)

                // Add the file
                files.add(File(path))

                // Release
                pbrDisplayName.let { Ole32.INSTANCE.CoTaskMemFree(it.value) }
                item.Release()
            }

            return files
        } finally {
            // Release
            itemArray?.Release()
        }
    }

    private fun HRESULT.verify(exceptionMessage: String): HRESULT {
        if (FAILED(this)) {
            throw RuntimeException(exceptionMessage)
        } else {
            return this
        }
    }

    private fun Window?.toHwnd(): WinDef.HWND? {
        return when (this) {
            null -> null
            else -> Native
                .getWindowPointer(this)
                .let { WinDef.HWND(it) }
        }
    }
}
