---
status: proposed
---

# Use One Typed JVM Dialog Parent

FileKit will represent JVM dialog ownership with one canonical `FileKitDialogParent` value. Public factories will distinguish AWT windows, Windows HWNDs, X11 XIDs, and Wayland exported handles; FileKit will not accept an untyped `Long`, `Any`, framework window object, or Nucleus/Tao handle. `FileKitDialogSettings.parentWindow` is replaced by `parent`; AWT callers migrate explicitly through `FileKitDialogParent.awt(window)`.

## Considered Options

- Adding a nullable `Long` beside `parentWindow` was rejected because the same primitive cannot safely distinguish an HWND, XID, NSView, `wl_surface*`, Tao handle, or Wayland exported token, and two stored parent fields create precedence and equality problems.
- Accepting framework objects such as `NucleusWindow` was rejected because it would couple FileKit's public interface to individual window frameworks.
- Keeping public parent variants was rejected because callers only need validated construction; opaque variants keep platform conversion and failure rules local to FileKit.
- Silently discarding an incompatible parent was rejected because a dialog that unexpectedly loses modality or stacking is a correctness failure.
- Preserving the 0.14.2 JVM constructor and data-class descriptors with compatibility overloads was rejected after a breaking change was explicitly accepted; it would require a regular class, version overloads, and manual value semantics for no behavioral benefit.

## Consequences

`FileKitDialogSettings` remains a data class with one `parent: FileKitDialogParent?` property. The 0.14.2 JVM constructor, `parentWindow` getter, destructuring, and `copy` descriptors break in 0.15.0; migration is direct and documented. Windows/Tao can be parented immediately. Nucleus/Tao on Linux still needs a public X11 XID or `xdg_foreign` exported handle, and macOS sheet parenting remains a separate design.
