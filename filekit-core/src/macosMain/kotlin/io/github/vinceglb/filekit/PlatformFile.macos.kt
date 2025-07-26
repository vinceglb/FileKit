package io.github.vinceglb.filekit

import platform.AppKit.NSWorkspace
import platform.AppKit.openFile
import platform.Foundation.NSFileManager

public actual fun PlatformFile.open() {
    val fileManager = NSFileManager.defaultManager
    val workspace = NSWorkspace.sharedWorkspace
    val absolutePath = absolutePath()
    if(fileManager.fileExistsAtPath(absolutePath)) {
        workspace.openFile(
            fullPath = absolutePath
        )
    } else {
        workspace.openURL(
            url = nsUrl
        )
    }
}