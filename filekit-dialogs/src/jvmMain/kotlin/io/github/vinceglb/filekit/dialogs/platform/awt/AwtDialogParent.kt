package io.github.vinceglb.filekit.dialogs.platform.awt

import io.github.vinceglb.filekit.dialogs.FileKitDialogParent
import io.github.vinceglb.filekit.dialogs.FileKitPickerException
import io.github.vinceglb.filekit.dialogs.requireAwtWindowOrNull
import java.awt.Dialog
import java.awt.Frame
import java.awt.Window

internal fun FileKitDialogParent?.resolveAwtFileDialogOwner(): Window? {
    val window = requireAwtWindowOrNull("AWT file dialogs") ?: return null
    if (!isSupportedAwtFileDialogOwner(window.javaClass)) {
        throw FileKitPickerException(
            "AWT file dialogs require an AWT Frame or Dialog parent.",
        )
    }
    return window
}

internal fun isSupportedAwtFileDialogOwner(
    windowClass: Class<out Window>,
): Boolean = Frame::class.java.isAssignableFrom(windowClass) ||
    Dialog::class.java.isAssignableFrom(windowClass)
