# JVM Dialog Parenting

This context describes how FileKit identifies the application window that owns a JVM file dialog. It distinguishes portable window objects from platform identifiers whose meaning and lifetime belong to a specific desktop system.

## Language

**Dialog Parent**:
A borrowed identity for the application window that owns a dialog for modality, focus, and stacking.
_Avoid_: Parent window handle, native handle

**AWT Parent**:
A dialog parent represented by a live `java.awt.Window`.
_Avoid_: Classic parent, Compose parent

**Native Parent Identifier**:
A borrowed, platform-specific identity that the operating system or desktop portal accepts as a dialog parent.
_Avoid_: Generic handle, raw handle

**Windows Owner Handle**:
A nonzero Win32 HWND identifying the window that owns a Windows dialog.
_Avoid_: Windows pointer, Tao handle

**X11 Window ID**:
A nonzero XID identifying an X11 window.
_Avoid_: X11 handle, decimal window ID

**Wayland Exported Handle**:
An opaque string issued by the `xdg_foreign` protocol for a live exported surface.
_Avoid_: Wayland pointer, `wl_surface` handle

**Portal Parent String**:
The complete XDG portal value serialized by FileKit: `""` for no parent, `x11:<bare lowercase hexadecimal XID>`, or `wayland:<opaque exported handle>`.
_Avoid_: Native handle, raw portal handle

**Tao Handle**:
An opaque Nucleus/Tao event-loop identity that is not, by itself, a native parent identifier.
_Avoid_: Native window handle

**Unparented Dialog**:
A dialog opened without an owner identity.
_Avoid_: Root dialog, default parent
