# FileKit

FileKit provides framework-independent file operations and native dialogs across platforms. This glossary names the ownership concepts shared by dialog implementations and framework adapters.

## Dialog ownership

**Framework adapter**:
An application-boundary conversion from a framework-specific window identity into a FileKit dialog parent. It remains outside the framework-independent dialog API.
_Avoid_: Framework integration layer

**Dialog parent**:
The borrowed identity of the application window that owns a native dialog. The parent remains owned by the caller and valid until the dialog operation completes.
_Avoid_: Parent window, native parent

**Native window identity**:
The platform-specific value that identifies a dialog parent to the operating system or desktop portal.
_Avoid_: Native handle

**Wayland parent export**:
A compositor-issued capability that lets the desktop portal associate dialogs with a Wayland window. The caller keeps the export active while that window may own dialogs and releases it when the window is disposed.
_Avoid_: Wayland window ID, surface pointer

**Unparented dialog**:
A native dialog with no owning application window, including when a framework has no supported or currently available identity. This is an explicit absence of a dialog parent, not a fallback for an invalid or incompatible value.
_Avoid_: Default parent
