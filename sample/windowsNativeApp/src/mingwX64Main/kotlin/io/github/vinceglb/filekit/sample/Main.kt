@file:OptIn(ExperimentalForeignApi::class)

package io.github.vinceglb.filekit.sample

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.documentsDir
import io.github.vinceglb.filekit.downloadsDir
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.lastModified
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.picturesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.writeString
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import kotlinx.cinterop.wcstr
import kotlinx.coroutines.runBlocking
import platform.windows.BS_PUSHBUTTON
import platform.windows.COLOR_WINDOW
import platform.windows.CS_HREDRAW
import platform.windows.CS_VREDRAW
import platform.windows.CW_USEDEFAULT
import platform.windows.CreateWindowExW
import platform.windows.DefWindowProcW
import platform.windows.DispatchMessageW
import platform.windows.ES_AUTOVSCROLL
import platform.windows.ES_MULTILINE
import platform.windows.ES_READONLY
import platform.windows.GetMessageW
import platform.windows.GetModuleHandleW
import platform.windows.HWND
import platform.windows.IDC_ARROW
import platform.windows.LPARAM
import platform.windows.LRESULT
import platform.windows.LoadCursorW
import platform.windows.MSG
import platform.windows.PostQuitMessage
import platform.windows.RegisterClassExW
import platform.windows.SW_SHOWDEFAULT
import platform.windows.SendMessageW
import platform.windows.ShowWindow
import platform.windows.TranslateMessage
import platform.windows.UINT
import platform.windows.UpdateWindow
import platform.windows.WM_CLOSE
import platform.windows.WM_COMMAND
import platform.windows.WM_CREATE
import platform.windows.WM_DESTROY
import platform.windows.WM_SETFONT
import platform.windows.WNDCLASSEXW
import platform.windows.WPARAM
import platform.windows.WS_CHILD
import platform.windows.WS_EX_CLIENTEDGE
import platform.windows.WS_OVERLAPPEDWINDOW
import platform.windows.WS_TABSTOP
import platform.windows.WS_VISIBLE
import platform.windows.WS_VSCROLL

// Button IDs
private const val ID_BTN_PICK_FILE = 101
private const val ID_BTN_PICK_IMAGE = 102
private const val ID_BTN_PICK_DIR = 103
private const val ID_BTN_SAVE_FILE = 104
private const val ID_BTN_WRITE_READ = 105
private const val ID_BTN_LIST_DIR = 106
private const val ID_BTN_OPEN_DEFAULT = 107
private const val ID_BTN_SHOW_DIRS = 108
private const val ID_EDIT_LOG = 201

private var hLog: HWND? = null
private var hDefaultFont: platform.windows.HFONT? = null

fun main() {
    FileKit.init("io.github.vinceglb.filekit.sample")

    memScoped {
        val hInstance = GetModuleHandleW(null)
        val className = "FileKitSampleWindow"

        // Register window class
        val wc = alloc<WNDCLASSEXW>()
        wc.cbSize = kotlinx.cinterop.sizeOf<WNDCLASSEXW>().toUInt()
        wc.style = (CS_HREDRAW or CS_VREDRAW).toUInt()
        wc.lpfnWndProc = staticCFunction(::wndProc)
        wc.hInstance = hInstance
        wc.hCursor = LoadCursorW(null, IDC_ARROW?.reinterpret())
        wc.hbrBackground = (COLOR_WINDOW + 1).toLong().toCPointer()
        wc.lpszClassName = className.wcstr.ptr

        RegisterClassExW(wc.ptr)

        // Create main window
        val hwnd = CreateWindowExW(
            dwExStyle = 0u,
            lpClassName = className,
            lpWindowName = "FileKit - Windows Native Sample",
            dwStyle = WS_OVERLAPPEDWINDOW.toUInt(),
            X = CW_USEDEFAULT,
            Y = CW_USEDEFAULT,
            nWidth = 720,
            nHeight = 620,
            hWndParent = null,
            hMenu = null,
            hInstance = hInstance,
            lpParam = null,
        )

        ShowWindow(hwnd, SW_SHOWDEFAULT)
        UpdateWindow(hwnd)

        // Message loop
        val msg = alloc<MSG>()
        while (GetMessageW(msg.ptr, null, 0u, 0u) != 0) {
            TranslateMessage(msg.ptr)
            DispatchMessageW(msg.ptr)
        }
    }
}

@Suppress("UNUSED_PARAMETER")
private fun wndProc(hwnd: HWND?, uMsg: UINT, wParam: WPARAM, lParam: LPARAM): LRESULT {
    when (uMsg.toInt()) {
        WM_CREATE -> {
            createUI(hwnd!!)
            return 0
        }

        WM_COMMAND -> {
            val id = wParam.toInt() and 0xFFFF
            when (id) {
                ID_BTN_PICK_FILE -> onPickFile()
                ID_BTN_PICK_IMAGE -> onPickImage()
                ID_BTN_PICK_DIR -> onPickDirectory()
                ID_BTN_SAVE_FILE -> onSaveFile()
                ID_BTN_WRITE_READ -> onWriteRead()
                ID_BTN_LIST_DIR -> onListDir()
                ID_BTN_OPEN_DEFAULT -> onOpenDefault()
                ID_BTN_SHOW_DIRS -> onShowDirs()
            }
            return 0
        }

        WM_CLOSE -> {
            platform.windows.DestroyWindow(hwnd)
            return 0
        }

        WM_DESTROY -> {
            PostQuitMessage(0)
            return 0
        }
    }
    return DefWindowProcW(hwnd, uMsg, wParam, lParam)
}

private fun createUI(parent: HWND) {
    val hInstance = GetModuleHandleW(null)

    // Create a default font
    hDefaultFont = platform.windows.CreateFontW(
        -14,
        0,
        0,
        0,
        400, // FW_NORMAL
        0u,
        0u,
        0u,
        0u,
        0u,
        0u,
        0u,
        0u,
        "Segoe UI",
    )

    var y = 10
    val btnWidth = 200
    val btnHeight = 32
    val spacing = 38

    // Left column: buttons
    createButton(parent, hInstance, "Pick a File", ID_BTN_PICK_FILE, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Pick an Image", ID_BTN_PICK_IMAGE, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Pick a Directory", ID_BTN_PICK_DIR, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Save a File", ID_BTN_SAVE_FILE, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Write & Read File", ID_BTN_WRITE_READ, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "List Directory", ID_BTN_LIST_DIR, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Open with Default App", ID_BTN_OPEN_DEFAULT, 15, y, btnWidth, btnHeight)
    y += spacing
    createButton(parent, hInstance, "Show Platform Dirs", ID_BTN_SHOW_DIRS, 15, y, btnWidth, btnHeight)

    // Right side: log area
    hLog = CreateWindowExW(
        dwExStyle = WS_EX_CLIENTEDGE.toUInt(),
        lpClassName = "EDIT",
        lpWindowName = "",
        dwStyle = (WS_CHILD or WS_VISIBLE or WS_VSCROLL or ES_MULTILINE or ES_AUTOVSCROLL or ES_READONLY).toUInt(),
        X = 230,
        Y = 10,
        nWidth = 460,
        nHeight = 555,
        hWndParent = parent,
        hMenu = ID_EDIT_LOG.toLong().toCPointer(),
        hInstance = hInstance,
        lpParam = null,
    )
    setFont(hLog!!)

    log("=== FileKit Windows Native Sample ===\r\nClick a button to try the API.\r\n")
}

private fun createButton(
    parent: HWND,
    hInstance: platform.windows.HINSTANCE?,
    text: String,
    id: Int,
    x: Int,
    y: Int,
    w: Int,
    h: Int,
) {
    val btn = CreateWindowExW(
        dwExStyle = 0u,
        lpClassName = "BUTTON",
        lpWindowName = text,
        dwStyle = (WS_TABSTOP or WS_VISIBLE or WS_CHILD or BS_PUSHBUTTON.toInt()).toUInt(),
        X = x,
        Y = y,
        nWidth = w,
        nHeight = h,
        hWndParent = parent,
        hMenu = id.toLong().toCPointer(),
        hInstance = hInstance,
        lpParam = null,
    )
    setFont(btn!!)
}

private fun setFont(hwnd: HWND) {
    hDefaultFont?.let {
        SendMessageW(hwnd, WM_SETFONT.toUInt(), it.toLong().toULong(), 1L)
    }
}

private fun log(text: String) {
    val h = hLog ?: return
    // Append text to the edit control
    val len = platform.windows.GetWindowTextLengthW(h).toLong()
    SendMessageW(h, platform.windows.EM_SETSEL.toUInt(), len.toULong(), len)
    memScoped {
        SendMessageW(h, platform.windows.EM_REPLACESEL.toUInt(), 0u, text.wcstr.ptr.toLong())
    }
    // Scroll to bottom
    SendMessageW(h, platform.windows.EM_SCROLLCARET.toUInt(), 0u, 0)
}

// --- Button handlers ---

private fun onPickFile() = runBlocking {
    log("--- Pick a File ---\r\n")
    val file = FileKit.openFilePicker(
        dialogSettings = FileKitDialogSettings(title = "Pick any file"),
    )
    if (file != null) {
        logFileInfo(file)
    } else {
        log("Cancelled.\r\n")
    }
    log("\r\n")
}

private fun onPickImage() = runBlocking {
    log("--- Pick an Image ---\r\n")
    val file = FileKit.openFilePicker(
        type = FileKitType.Image,
        dialogSettings = FileKitDialogSettings(title = "Pick an image"),
    )
    if (file != null) {
        logFileInfo(file)
    } else {
        log("Cancelled.\r\n")
    }
    log("\r\n")
}

private fun onPickDirectory() = runBlocking {
    log("--- Pick a Directory ---\r\n")
    val dir = FileKit.openDirectoryPicker(
        dialogSettings = FileKitDialogSettings(title = "Pick a directory"),
    )
    if (dir != null) {
        log("Path: ${dir.path}\r\n")
        log("isDirectory: ${dir.isDirectory()}\r\n")
    } else {
        log("Cancelled.\r\n")
    }
    log("\r\n")
}

private fun onSaveFile() = runBlocking {
    log("--- Save a File ---\r\n")
    val file = FileKit.openFileSaver(
        suggestedName = "filekit-sample",
        extension = "txt",
        dialogSettings = FileKitDialogSettings(title = "Save a file"),
    )
    if (file != null) {
        file.writeString("Hello from FileKit Windows Native!")
        log("Saved to: ${file.path}\r\n")
        log("Content: \"Hello from FileKit Windows Native!\"\r\n")
    } else {
        log("Cancelled.\r\n")
    }
    log("\r\n")
}

private fun onWriteRead() = runBlocking {
    log("--- Write & Read ---\r\n")
    val testFile = FileKit.filesDir.resolve("test.txt")
    val content = "Hello from FileKit!\r\nWindows Native target."

    testFile.writeString(content)
    log("Written to: ${testFile.path}\r\n")

    val readBack = testFile.readString()
    log("Read back: $readBack\r\n")
    log("Size: ${testFile.size()} bytes\r\n")
    log("Last modified: ${testFile.lastModified()}\r\n")
    log("\r\n")
}

private fun onListDir() = runBlocking {
    log("--- List Directory ---\r\n")
    val dir = FileKit.openDirectoryPicker(
        dialogSettings = FileKitDialogSettings(title = "Select directory to list"),
    )
    if (dir == null) {
        log("Cancelled.\r\n\r\n")
        return@runBlocking
    }

    log("${dir.path}:\r\n")
    val files = dir.list()
    if (files.isEmpty()) {
        log("  (empty)\r\n")
    } else {
        files.take(15).forEach { file ->
            val tag = if (file.isDirectory()) "DIR " else "FILE"
            log("  [$tag] ${file.name}\r\n")
        }
        if (files.size > 15) {
            log("  ... +${files.size - 15} more\r\n")
        }
    }
    log("\r\n")
}

private fun onOpenDefault() = runBlocking {
    log("--- Open with Default App ---\r\n")
    val file = FileKit.openFilePicker(
        dialogSettings = FileKitDialogSettings(title = "Pick a file to open"),
    )
    if (file != null) {
        log("Opening: ${file.path}\r\n")
        FileKit.openFileWithDefaultApplication(file)
    } else {
        log("Cancelled.\r\n")
    }
    log("\r\n")
}

private fun onShowDirs() {
    log("--- Platform Directories ---\r\n")
    log("filesDir:     ${FileKit.filesDir.path}\r\n")
    log("cacheDir:     ${FileKit.cacheDir.path}\r\n")
    log("downloadsDir: ${FileKit.downloadsDir.path}\r\n")
    log("documentsDir: ${FileKit.documentsDir.path}\r\n")
    log("picturesDir:  ${FileKit.picturesDir.path}\r\n")
    log("\r\n")
}

private fun logFileInfo(file: PlatformFile) {
    log("Name:         ${file.name}\r\n")
    log("Extension:    ${file.extension}\r\n")
    log("Path:         ${file.path}\r\n")
    log("Size:         ${file.size()} bytes\r\n")
    log("Exists:       ${file.exists()}\r\n")
    log("MIME type:    ${file.mimeType()}\r\n")
    log("Last modified: ${file.lastModified()}\r\n")
}
