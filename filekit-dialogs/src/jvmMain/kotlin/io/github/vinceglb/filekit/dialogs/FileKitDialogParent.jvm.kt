package io.github.vinceglb.filekit.dialogs

import java.awt.Window

/**
 * The JVM window or platform handle which owns a FileKit dialog.
 *
 * Use a factory matching the integration which owns the window. Native handles are
 * deliberately typed by platform: a Wayland token is not a pointer and an X11 XID
 * is not a Windows HWND.
 */
public sealed class FileKitDialogParent private constructor() {
    internal class Awt(
        val window: Window,
    ) : FileKitDialogParent()

    internal class Windows(
        val hwnd: Long,
    ) : FileKitDialogParent()

    internal class X11(
        val identifier: String,
    ) : FileKitDialogParent()

    internal class Wayland(
        val identifier: String,
    ) : FileKitDialogParent()

    public companion object {
        private val POINTER_SHAPED_TOKEN: Regex = Regex("[+-]?(?:0[xX][0-9a-fA-F]+|[0-9]+)")

        /** Uses an AWT window as the dialog parent. */
        public fun awt(window: Window): FileKitDialogParent = Awt(window)

        /**
         * Uses a non-zero Windows HWND as the dialog parent.
         *
         * Negative values are accepted because they can be valid pointer bit patterns.
         */
        public fun windows(hwnd: Long): FileKitDialogParent {
            require(hwnd != 0L) { "A Windows HWND must not be zero." }
            return Windows(hwnd)
        }

        /** Uses a non-zero 32-bit X11 XID as the dialog parent. */
        public fun x11(xid: Long): FileKitDialogParent = X11(x11Identifier(xid))

        /**
         * Uses an opaque xdg-portal/xdg-foreign Wayland parent token.
         *
         * The token must come from the caller's Wayland integration. FileKit never
         * accepts or dereferences a raw `wl_surface*` pointer.
         */
        public fun wayland(portalToken: String): FileKitDialogParent {
            require(portalToken.isNotBlank()) { "A Wayland portal token must not be blank." }
            require(portalToken.none(Char::isWhitespace)) {
                "A Wayland portal token must not contain whitespace."
            }
            require(!portalToken.startsWith("wayland:")) {
                "Pass the opaque Wayland portal token without the wayland: prefix."
            }
            require(!portalToken.matches(POINTER_SHAPED_TOKEN)) {
                "A Wayland portal token must not be a raw pointer-shaped value."
            }
            return Wayland("wayland:$portalToken")
        }
    }
}

internal fun FileKitDialogParent?.awtWindowOrNull(): Window? = when (this) {
    null -> null
    is FileKitDialogParent.Awt -> window
    else -> null
}

internal fun FileKitDialogParent?.requireAwtWindowOrNull(adapter: String): Window? = when (this) {
    null -> null
    is FileKitDialogParent.Awt -> window
    else -> throw IllegalArgumentException("$adapter only supports an AWT dialog parent.")
}

internal fun FileKitDialogParent?.resolveWindowsHandle(
    awtWindowHandle: (Window) -> Long,
): Long? = when (this) {
    null -> null
    is FileKitDialogParent.Awt -> awtWindowHandle(window).requireNonZeroHandle("An AWT window")
    is FileKitDialogParent.Windows -> hwnd
    else -> throw IllegalArgumentException("Windows dialogs only support AWT or Windows dialog parents.")
}

internal fun FileKitDialogParent?.resolveXdgPortalParent(
    awtWindowXid: (Window) -> Long,
): String = when (this) {
    null -> ""
    is FileKitDialogParent.Awt -> x11Identifier(awtWindowXid(window))
    is FileKitDialogParent.X11 -> identifier
    is FileKitDialogParent.Wayland -> identifier
    else -> throw IllegalArgumentException("XDG portal dialogs only support AWT, X11, or Wayland dialog parents.")
}

private fun x11Identifier(xid: Long): String {
    require(xid in 1..X11_XID_MAX) { "An X11 XID must be between 1 and 0xffffffff." }
    return "x11:${xid.toString(16)}"
}

private fun Long.requireNonZeroHandle(label: String): Long {
    require(this != 0L) { "$label must resolve to a non-zero native handle." }
    return this
}

private const val X11_XID_MAX: Long = 0xffff_ffffL
