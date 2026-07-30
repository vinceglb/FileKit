package io.github.vinceglb.filekit.dialogs

import java.awt.Window

/**
 * A borrowed JVM window identity that owns a FileKit dialog.
 *
 * Keep the represented window, native window, or Wayland export alive until the
 * suspending picker call completes. FileKit never owns or destroys the parent.
 *
 * Create instances with [awt], [windows], [x11], or [wayland]. The concrete
 * variants are intentionally hidden so a native identifier cannot be confused
 * with an identifier from another window system.
 */
public sealed class FileKitDialogParent {
    internal data class Awt(
        internal val window: Window,
    ) : FileKitDialogParent() {
        override fun toString(): String = "FileKitDialogParent.Awt"
    }

    internal data class Windows(
        internal val hwnd: Long,
    ) : FileKitDialogParent() {
        override fun toString(): String = "FileKitDialogParent.Windows"
    }

    internal data class X11(
        internal val xid: Long,
    ) : FileKitDialogParent() {
        override fun toString(): String = "FileKitDialogParent.X11"
    }

    internal data class Wayland(
        internal val exportedHandle: String,
    ) : FileKitDialogParent() {
        override fun toString(): String = "FileKitDialogParent.Wayland"
    }

    public companion object {
        /**
         * Uses a live AWT [Window] as the dialog parent.
         */
        public fun awt(window: Window): FileKitDialogParent = Awt(window)

        /**
         * Uses a non-zero Windows HWND as the dialog parent.
         *
         * Negative values are accepted because a signed [Long] can carry a pointer
         * bit pattern whose high bit is set.
         */
        public fun windows(hwnd: Long): FileKitDialogParent {
            require(hwnd != 0L) { "A Windows HWND must not be zero." }
            return Windows(hwnd)
        }

        /**
         * Uses an unsigned 32-bit X11 XID as the dialog parent.
         */
        public fun x11(xid: Long): FileKitDialogParent {
            require(xid in 1L..X11_XID_MAX) {
                "An X11 XID must be between 1 and 0xffffffff."
            }
            return X11(xid)
        }

        /**
         * Uses an unprefixed `xdg_foreign` Wayland exported handle.
         *
         * The handle is opaque. FileKit only rejects an empty value or an embedded
         * NUL, then adds the `wayland:` portal prefix without normalizing the text.
         * A raw `wl_surface*` pointer is not a valid exported handle.
         */
        public fun wayland(exportedHandle: String): FileKitDialogParent {
            require(exportedHandle.isNotEmpty()) {
                "A Wayland exported handle must not be empty."
            }
            require('\u0000' !in exportedHandle) {
                "A Wayland exported handle must not contain NUL."
            }
            return Wayland(exportedHandle)
        }
    }
}

internal fun FileKitDialogParent?.resolveWindowsHandle(
    awtWindowHandle: (Window) -> Long,
): Long? = when (this) {
    null -> null

    is FileKitDialogParent.Awt -> resolveAwtNativeIdentifier("Windows HWND") {
        awtWindowHandle(window)
    }

    is FileKitDialogParent.Windows -> hwnd

    else -> unsupportedParent(
        adapter = "Windows dialogs",
        supported = "AWT or Windows",
    )
}

internal fun FileKitDialogParent?.resolveXdgPortalParent(
    awtWindowXid: (Window) -> Long,
): String = when (this) {
    null -> {
        ""
    }

    is FileKitDialogParent.Awt -> {
        val xid = resolveAwtNativeIdentifier("X11 XID") {
            awtWindowXid(window)
        }
        x11PortalParent(xid)
    }

    is FileKitDialogParent.X11 -> {
        x11PortalParent(xid)
    }

    is FileKitDialogParent.Wayland -> {
        "wayland:$exportedHandle"
    }

    else -> {
        unsupportedParent(
            adapter = "XDG portal dialogs",
            supported = "AWT, X11, or Wayland",
        )
    }
}

internal fun FileKitDialogParent?.requireAwtWindowOrNull(
    adapter: String,
): Window? = when (this) {
    null -> null

    is FileKitDialogParent.Awt -> window

    else -> unsupportedParent(
        adapter = adapter,
        supported = "AWT",
    )
}

internal fun FileKitDialogParent?.requireMacOSCompatible() {
    when (this) {
        null, is FileKitDialogParent.Awt -> Unit

        else -> unsupportedParent(
            adapter = "macOS dialogs",
            supported = "AWT",
        )
    }
}

internal fun resolveAwtNativeIdentifier(
    identifierName: String,
    conversion: () -> Long,
): Long {
    val identifier = try {
        conversion()
    } catch (cause: Exception) {
        throw FileKitPickerException(
            message = "The AWT dialog parent could not resolve to a usable $identifierName.",
            cause = cause,
        )
    }

    if (identifier == 0L) {
        throw FileKitPickerException(
            "The AWT dialog parent resolved to an invalid zero $identifierName.",
        )
    }

    return identifier
}

private fun x11PortalParent(xid: Long): String = "x11:${xid.toString(16)}"

private fun FileKitDialogParent.unsupportedParent(
    adapter: String,
    supported: String,
): Nothing = throw FileKitPickerException(
    "$adapter does not support ${kindName()} dialog parents. Supported parents: $supported.",
)

private fun FileKitDialogParent.kindName(): String = when (this) {
    is FileKitDialogParent.Awt -> "AWT"
    is FileKitDialogParent.Windows -> "Windows"
    is FileKitDialogParent.X11 -> "X11"
    is FileKitDialogParent.Wayland -> "Wayland"
}

private const val X11_XID_MAX: Long = 0xffff_ffffL
