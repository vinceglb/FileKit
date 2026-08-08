# FileKit

FileKit provides multiplatform file access and system-mediated file interactions through a consistent, framework-independent interface. This glossary names the interaction and ownership concepts shared by dialog implementations and framework adapters.

## Dialog interactions

**Dialog operation**:
A FileKit-mediated system interaction for picking, selecting a directory, choosing a save destination, capturing media, or sharing files.
_Avoid_: Launcher operation, picker operation when referring to all dialog kinds

**Operational failure**:
An expected inability to complete a valid dialog operation, represented to callers as a FileKit-owned dialog failure rather than an incidental platform failure.
_Avoid_: Platform exception, unexpected defect

**Invalid invocation**:
A dialog request that violates a caller-controlled precondition, such as required initialization, valid arguments, or a documented argument combination. It is a caller-contract violation rather than an operational failure.
_Avoid_: Operational failure, platform failure

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
