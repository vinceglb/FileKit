# Unified JVM Dialog Parent

## Status

Validated after five review rounds by Claude Opus 5 and GPT-5.6-sol (high).

Request: [Kotlin Slack Nucleus 2.0 thread](https://kotlinlang.slack.com/archives/C0BJ0GTE2/p1784024682833589?thread_ts=1783938008.832499&cid=C0BJ0GTE2)

Prior prototype: [PR #624](https://github.com/vinceglb/FileKit/pull/624), closed without merge

Baseline: `origin/main` at `1c0d166f786e9ddf17e8f6fd814cd46aa12f8761`

Target release: `0.15.0`

Compatibility decision: a breaking JVM API migration is explicitly accepted to keep the model small and direct.

## Objective

Allow JVM file dialogs to be owned by non-AWT windows without coupling FileKit to Nucleus or conflating platform identifiers. Keep the existing AWT capability through an explicit typed adapter, but replace the old `parentWindow` settings property rather than preserving its source or binary shape.

The first practical integration is a Nucleus 2.1.10 Tao window on Windows, where Nucleus exposes a Win32 HWND. The public FileKit model must also correctly represent X11 and Wayland portal parents so the interface does not become Windows-specific.

## Verified Problem and Constraints

- `FileKitDialogSettings` currently stores only `parentWindow: java.awt.Window?`.
- Windows passes that AWT window through JNA to obtain an HWND for `IModalWindow.Show`.
- The Linux XDG portal requires:
  - `x11:<XID>`, with the XID written in hexadecimal; or
  - `wayland:<HANDLE>`, where the handle is an opaque token obtained from `xdg_foreign`.
- FileKit currently emits `X11:<decimal>`, which does not follow the documented XDG format.
- A raw Wayland `wl_surface*` is not an XDG portal parent. The exported handle's text has no documented grammar beyond being an opaque string, so FileKit must not reject it with pointer-shape heuristics.
- Linux falls back from the XDG portal to AWT/Swing. Those fallbacks can consume an AWT window but cannot consume a Tao, X11, or Wayland parent identifier.
- macOS/JVM currently calls `runModal()` and ignores the AWT parent. True window-modal presentation would require an NSWindow and the asynchronous sheet lifecycle.
- The resolved Nucleus 2.1.10 sources expose:
  - `NucleusWindow.unsafe.awtWindow` for the AWT backend;
  - `NucleusWindow.unsafe.taoWindow`;
  - `TaoWindow.nativeHandle`, which is an HWND on Windows, an NSView pointer on macOS, and `0` on Linux;
  - `NucleusWindow.unsafe.taoHandle`, which is an opaque Tao event-loop identity rather than an OS dialog-parent identifier.
- Nucleus 2.1.10 therefore enables a FileKit Tao parent directly on Windows only. Linux requires a future public Nucleus portal-parent contract, and macOS needs a separate FileKit sheet design.
- `FileKitDialogSettings` 0.14.2 is a JVM data class whose second property is `parentWindow`. Replacing it with `parent` changes the constructor, getter, destructuring, `copy`, and JVM descriptors. That break is intentional for 0.15.0.
- Existing AWT callers migrate from `parentWindow = window` to `parent = FileKitDialogParent.awt(window)`.
- The current documentation's `FileKitDialogSettings(this.window)` example does not match the 0.14.2 constructor order and must be corrected, not preserved.
- PR #624 proved the platform forwarding seams and CI viability, but its overload-based settings design left positional `null` ambiguous, gave AWT wrappers allocation identity instead of value equality, and contained a nullable-unsafe replacement expression.

Primary references:

- [XDG portal window identifiers](https://flatpak.github.io/xdg-desktop-portal/docs/window-identifiers.html)
- [XDG FileChooser portal](https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.FileChooser.html)
- [Windows `IModalWindow::Show`](https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nf-shobjidl_core-imodalwindow-show)
- [Nucleus 2.1.10 tag](https://github.com/NucleusFramework/Nucleus/tree/v2.1.10)

## Autonomous Grilling Record

The requested grilling session was resolved without user interruption. Facts were checked against the repository, the current resolved Nucleus 2.1.10 source JARs, and platform documentation. The initial compatibility-heavy design was discarded after the explicit decision to accept a breaking change. A temporary Kotlin 2.4.10 prototype compiled the final sealed/internal hierarchy without warnings.

### 1. Is this a Nucleus-specific integration?

**Recommendation and decision:** No. Treat Nucleus as the first consumer of a framework-neutral FileKit seam.

FileKit should accept the exact identity required by its platform adapter. A dependency on `NucleusWindow`, Tao, Compose Desktop, LWJGL, or another framework would move third-party knowledge into FileKit's public interface and require a new adapter for every framework.

### 2. Should FileKit add a second raw handle beside `parentWindow`?

**Recommendation and decision:** No. Store one canonical `FileKitDialogParent?`.

Separate `parentWindow` and `nativeParent` values require precedence rules, create invalid combinations, and make equality and copy semantics unstable. A generic `Long` also cannot represent Wayland's string token and cannot distinguish an HWND, XID, NSView, native pointer, or Tao handle.

### 3. What should the public interface expose?

**Recommendation and decision:** A public opaque `FileKitDialogParent` with validated companion factories:

```kotlin
FileKitDialogParent.awt(window)
FileKitDialogParent.windows(hwnd)
FileKitDialogParent.x11(xid)
FileKitDialogParent.wayland(exportedHandle)
```

Concrete variants are nested `internal` implementations of the sealed class. Callers outside the module can only use the factories; platform adapters inside FileKit can resolve the internal variants without reflection or public downcasts.

Do not add `native(Long)`, `handle(Any)`, public subclasses, or a caller-implemented resolver interface.

### 4. Does FileKit own the parent or its lifetime?

**Recommendation and decision:** No. Every parent is borrowed.

The caller must keep the AWT window, HWND, X11 window, or Wayland export alive until the suspending picker call completes. FileKit never destroys, dereferences beyond the required platform conversion, or extends the lifetime of the owning window. In particular, the Wayland exporter object must remain alive because destroying it invalidates the exported token.

### 5. How strict should factory validation be?

**Recommendation and decision:**

- AWT: accept a non-null `Window`.
- Windows: reject `0`; accept other `Long` bit patterns, including negative values that can represent a pointer's high bit.
- X11: accept `1..0xffffffff` and format it internally as bare lowercase hexadecimal, without `0x`, padding, or leading zeroes.
- Wayland: reject only an empty string and an embedded NUL. Treat every other character as opaque and add the `wayland:` portal prefix internally.

Document that `wayland()` receives the unprefixed `xdg_foreign` handle. Do not trim it, parse it, or reject decimal-, hexadecimal-, whitespace-, or pointer-shaped text: the protocol defines an opaque string, not a FileKit-owned grammar.

The validation asymmetry is intentional: the XDG contract defines an X11 XID as an unsigned 32-bit identifier, while an HWND is a pointer-width value carried in a Kotlin `Long`. FileKit range-checks the former but only rejects the null bit pattern for the latter.

Invalid factory arguments fail immediately with `IllegalArgumentException`. `FileKitPickerException` is reserved for a valid parent that cannot be used by the selected picker or resolved at dialog-open time.

### 6. What happens when a parent is incompatible with the active picker?

**Recommendation and decision:** Fail before opening the dialog with a descriptive `FileKitPickerException`.

- Windows accepts no parent, AWT, or Windows.
- The XDG portal accepts no parent, AWT/X11, X11, or Wayland.
- Linux's AWT `FileDialog` fallbacks accept no parent or an AWT `Frame`/`Dialog`; an AWT `Window` of another subtype fails rather than being silently cast to null. The Swing directory fallback accepts any AWT `Window`.
- macOS accepts no parent or AWT for compatibility; AWT remains application-modal because the current implementation uses `runModal()`.
- Every other pairing fails explicitly.

Silently opening an unparented dialog would make modality and stacking nondeterministic and would hide integration bugs.

On ownership-capable adapters, only an absent parent resolves to an unparented dialog. If JNA cannot obtain a usable HWND/XID from a supplied AWT window because it is unrealized, disposed, returns zero, or otherwise fails, throw a descriptive `FileKitPickerException`; retain the cause when conversion threw. Rejecting a zero identifier is a new deterministic check.

There are two documented exceptions to parent preservation:

- macOS accepts an AWT parent for migration compatibility but deliberately ignores it because `runModal()` is application-modal;
- Compose `WindowScope` overloads deliberately replace any caller-supplied parent with the scope's AWT window, preserving their existing contract.

### 7. Should this change Linux fallback selection?

**Recommendation and decision:** No.

Keep portal-first selection and the existing AWT/Swing fallbacks. If the portal is unavailable and the caller supplied a native X11 or Wayland parent, throw rather than falling back unparented. This is new behavior enabled by the new parent kinds and must be documented. A separate future change may add another native Linux picker, but this feature must not broaden picker selection.

### 8. Should macOS Tao parenting be included?

**Recommendation and decision:** No.

Nucleus exposes an NSView, while AppKit sheet presentation requires an owning NSWindow and a completion-handler lifecycle. FileKit's current synchronous `runModal()` implementation is application-modal and ignores `parentWindow`. Converting the NSView, bridging an AppKit block, suspending until completion, and handling cancellation/lifetime is a separate feature with different risks.

This plan preserves current macOS behavior and documents that neither AWT nor Tao establishes window-modal sheet ownership yet.

### 9. How should `FileKitDialogSettings` migrate?

**Recommendation and decision:** Make the breaking change directly and keep the type a data class:

```kotlin
public actual data class FileKitDialogSettings(
    public val title: String? = null,
    public val parent: FileKitDialogParent? = null,
    public val macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
)
```

Remove `parentWindow` completely. Do not add a deprecated adapter constructor, getter, version overload, or manually reproduced data-class members. AWT callers use `parent = FileKitDialogParent.awt(window)`.

The generated data-class behavior is the new contract: `component2()` and `copy` use `FileKitDialogParent?`, and equality/hash code operate on that canonical value.

### 10. What are the equality and diagnostic-string semantics?

**Recommendation and decision:** Parent variants compare structurally, but their strings expose only the parent kind.

- AWT parents wrapping the same `Window` compare equal.
- HWNDs, XIDs, and Wayland handles compare by value.
- `FileKitMacOSSettings` retains its existing reference equality.
- `FileKitDialogParent.toString()` returns a kind-only value such as `FileKitDialogParent.Windows`; it never prints an AWT object, HWND, XID, or Wayland token.
- Generated `FileKitDialogSettings.toString()` therefore remains useful without leaking borrowed identifiers.

Internal parent variants may be data classes with an explicit redacted `toString()`.

### 11. Which parent wins in Compose `WindowScope` extensions?

**Recommendation and decision:** The scope's AWT window remains authoritative.

The current JVM `WindowScope.remember*Launcher` overloads always replace the caller's parent with `this.window`. Preserve that behavior by copying the settings with `parent = FileKitDialogParent.awt(window)`. This also prevents a stale or platform-incompatible parent from escaping an AWT window scope.

### 12. How should Nucleus demonstrate the feature?

**Recommendation and decision:** Keep the FileKit library framework-neutral and wire only the existing `:sample:nucleusApp`.

The sample may depend directly on `projects.filekitDialogs` and adapt its `NucleusWindow` locally:

- AWT backend: `unsafe.awtWindow` can become `FileKitDialogParent.awt`.
- Tao/Windows: a nonzero `unsafe.taoWindow.nativeHandle` can become `FileKitDialogParent.windows`.
- Tao/Linux and Tao/macOS: return no parent and explain the current upstream/FileKit limitation.

Gate the native handle by Nucleus's current platform before conversion because the same property is an NSView on macOS. Do not pass `unsafe.taoHandle`.

Reuse the shared sample UI by threading a narrow dialog-settings transform from `nucleusApp`; do not duplicate a demonstration screen or add Nucleus to a published FileKit module.

### 13. Does this need an ADR and public documentation?

**Recommendation and decision:** Yes.

The single canonical parent is a hard-to-reverse public seam with non-obvious rejected alternatives, so record it in `specs/adr/0002-use-one-typed-jvm-dialog-parent.md`. Update the JVM dialog-settings documentation because the public construction model and platform limitations change. Keep the domain glossary in this specification directory.

## Public Interface Contract

Add in `jvmMain`:

```kotlin
public sealed class FileKitDialogParent {
    internal data class Awt(internal val window: Window) :
        FileKitDialogParent()

    internal data class Windows(internal val hwnd: Long) :
        FileKitDialogParent()

    internal data class X11(internal val xid: Long) :
        FileKitDialogParent()

    internal data class Wayland(internal val exportedHandle: String) :
        FileKitDialogParent()

    public companion object {
        public fun awt(window: Window): FileKitDialogParent
        public fun windows(hwnd: Long): FileKitDialogParent
        public fun x11(xid: Long): FileKitDialogParent
        public fun wayland(exportedHandle: String): FileKitDialogParent
    }
}
```

Replace JVM settings with:

```kotlin
public actual data class FileKitDialogSettings(
    public val title: String? = null,
    public val parent: FileKitDialogParent? = null,
    public val macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
)
```

There is no compatibility constructor or duplicate AWT property. This is the complete settings surface.

## Implementation Plan

### 1. Introduce the opaque parent module

Add:

- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/FileKitDialogParent.jvm.kt`

Implement:

1. Nested internal structural variants for AWT, Windows, X11, and Wayland, each with a kind-only `toString()`. The sealed class uses its default protected constructor, so the shown hierarchy compiles while external callers remain factory-only.
2. The four public factories and validation rules from the grilling record. Invalid arguments throw `IllegalArgumentException`.
3. Internal resolvers for:
   - AWT window projection;
   - Windows HWND resolution, using `Native.getWindowPointer(window)` plus `Pointer.nativeValue(...)` in production;
   - XDG portal identifier resolution, with `Native.getWindowID(window)` as the production AWT-to-XID converter and an injected converter for tests;
   - AWT-only fallback validation;
   - macOS compatibility validation.
4. Descriptive `FileKitPickerException` messages that identify the supplied parent kind and supported kinds without leaking raw pointer/token values.
5. AWT conversion failures are reported as `FileKitPickerException`. Preserve the original cause for a thrown JNA conversion; a zero result has no cause. An unrealized, disposed, zero-ID, or otherwise unusable AWT parent is not silently converted to an unparented dialog.
6. Put the common zero/exception mapping behind a window-free internal helper that accepts a `() -> Long`. Production passes the JNA calls above; unit tests inject success, zero, and throwing lambdas without constructing an AWT window.

The resolvers are the module's test surface. Platform pickers must not inspect parent variants directly.

### 2. Replace the JVM settings parent

Update:

- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/FileKitDialogSettings.jvm.kt`

Changes:

1. Keep the class as a data class.
2. Replace `parentWindow: Window?` in the second position with `parent: FileKitDialogParent?`.
3. Keep `title`, `macOS`, and `createDefault()` behavior unchanged.
4. Rely on generated `componentN`, `copy`, equality, hash code, and settings `toString`.
5. Update all repository callers in the same change.

Do not add a legacy constructor/getter, version overload, window-first constructor, public platform variants, or generic native handle.

### 3. Route each JVM picker through the canonical parent

Update:

- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/windows/WindowsFilePicker.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/xdg/XdgFilePickerPortal.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/linux/LinuxFilePicker.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/awt/AwtFilePicker.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/awt/AwtFileSaver.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/swing/SwingFilePicker.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/MacOSFilePicker.kt`

Changes:

1. Windows resolves the parent to `Long?`, converts it to `WinDef.HWND(Pointer(handle))`, and passes it to every Open, Multi, Directory, and Save `Show` call.
2. Preserve the dedicated `FileKit-Windows-Dialog` STA executor and `FOS_FORCEFILESYSTEM`.
3. Extract an internal Windows show boundary that accepts the already-resolved `HWND?` and an injected `Show` call. Production delegates to `FileDialog.Show`; JVM tests use a recording lambda and never initialize COM.
4. XDG resolves once to:
   - `""` for no parent;
   - `x11:<bare lowercase hexadecimal XID>` for AWT/X11, with no `0x`, padding, or leading zeroes;
   - `wayland:<opaque exported handle>` for Wayland.
5. Put D-Bus `OpenFile`/`SaveFile` calls behind a small internal XDG transport. Production uses the current D-Bus objects; tests record the complete parent identifier without connecting to a session bus.
6. Use the same resolver for both `OpenFile` and `SaveFile`.
7. Both `AwtFilePicker` and `AwtFileSaver` use one shared internal `resolveAwtFileDialogOwner` helper. It projects only `Frame`/`Dialog`; unsupported AWT `Window` subtypes fail. Extract the class predicate from instantiation so its accept/reject matrix is testable without constructing a window in a headless JVM. Swing projects any AWT `Window`. All non-AWT parents fail on either fallback.
8. Type the XDG, AWT, and Swing Linux delegates as `PlatformFilePicker`, allowing recording fakes without a mocking dependency. Production construction still supplies the existing concrete pickers.
9. Inject portal availability separately as `{ xdgFilePickerPortal.isAvailable() }`, and cache its result with the existing lazy behavior.
10. Linux selects the same portal/fallback implementation as before; native parents fail if only an AWT/Swing fallback is available.
11. macOS validates that no non-AWT native parent was supplied, then retains `runModal()`.

Keep cancellation, result extraction, COM balancing, D-Bus response handling, native picker threading, and file-system option behavior unchanged.

### 4. Preserve Compose AWT injection

Update:

- `filekit-dialogs-compose/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/compose/FileKitCompose.jvm.kt`

Change the existing private `injectDialogSettings` function to `internal` and make it accept an already-created `FileKitDialogParent` rather than a `Window`. Each `WindowScope` overload passes `FileKitDialogParent.awt(this.window)`; the helper copies or creates settings with that value. Add `filekit-dialogs-compose/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/compose/FileKitComposeJvmTest.kt` to call the helper with a native parent and prove both null settings and an existing parent are replaced without constructing an AWT window. The convention plugin already wires `jvmTest` to `commonTest`, which provides `kotlin.test`; no new test dependency is needed.

### 5. Wire the existing Nucleus sample

Update:

- `sample/nucleusApp/build.gradle.kts`
- `sample/nucleusApp/src/main/kotlin/io/github/vinceglb/filekit/sample/nucleus/Main.kt`
- the narrow shared-sample files needed to pass a dialog-settings transform

Changes:

1. Add a direct `projects.filekitDialogs` dependency.
2. Read the current `nucleusWindow` inside `DecoratedWindow`.
3. Convert AWT and Tao/Windows parents locally; never pass `taoHandle`.
4. Pass the result through a settings transform to the existing shared picker UI and use the plain, non-`WindowScope` launcher path so Compose does not replace the Tao HWND.
5. Leave Tao/Linux and Tao/macOS unparented with a source comment explaining why.

Do not add a Nucleus adapter to a published FileKit module or duplicate the sample UI.

### 6. Update public documentation

Update:

- `docs/dialogs/dialog-settings.mdx`
- public KDoc on `FileKitDialogParent` and `FileKitDialogSettings`

Document:

1. Canonical AWT, Windows, X11, and Wayland construction examples.
2. The platform compatibility matrix, including Linux fallback failure.
3. Borrowed lifetime requirements.
4. The difference between an unprefixed Wayland exported handle, raw `wl_surface*`, and Tao handle; FileKit adds the `wayland:` prefix and does not normalize caller input.
5. The breaking AWT migration from `parentWindow = window` to `parent = FileKitDialogParent.awt(window)`.
6. A Nucleus/Tao Windows example gated by platform.
7. Current Nucleus Linux and macOS limitations.
8. The existing macOS `runModal()` behavior.
9. A corrected Compose example using `parent = FileKitDialogParent.awt(this.window)`.
10. Factory validation (`IllegalArgumentException`), picker-resolution failures (`FileKitPickerException`), and the new Linux failure when a native parent cannot use the portal fallback.

Do not claim cross-platform Tao parenting until Nucleus exposes a compatible Linux identifier and FileKit supports macOS sheets.

## Test Plan

### Parent model tests

Add:

- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/FileKitDialogParentTest.kt`

Cover:

1. AWT structural equality for the same `Window` when a graphics environment is available.
2. Windows zero rejection as `IllegalArgumentException` and preservation of positive/negative nonzero bit patterns.
3. X11 zero/negative/out-of-range rejection as `IllegalArgumentException`.
4. Wayland empty/NUL rejection as `IllegalArgumentException`.
5. Wayland opacity using the XDG documentation example plus whitespace-, decimal-, hexadecimal-shaped, and already-`wayland:`-prefixed tokens.
6. Correct no-parent result.
7. Explicit platform mismatch failures.
8. AWT-only fallback and macOS compatibility rules.
9. Kind-only `toString()` for every parent variant, with no raw identifier or AWT object text.

### Settings model tests

Add:

- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/FileKitDialogSettingsTest.kt`

Runtime/value cases:

1. Defaults produce no parent.
2. AWT and native parents participate in data-class equality and hash code; equality fixtures reuse the same `FileKitMacOSSettings` instance because that class intentionally has reference equality.
3. Copying only title/macOS preserves the parent.
4. `copy(parent = null)` clears any parent.
5. `component2()` returns the canonical parent.
6. Settings `toString()` contains the parent kind but not its raw value.

### Platform forwarding tests

Add or extend:

- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/windows/WindowsDialogParentTest.kt`
- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/xdg/XdgDialogParentTest.kt`
- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/awt/AwtDialogOwnerTest.kt`
- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/linux/LinuxFilePickerTest.kt`
- `filekit-dialogs-compose/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/compose/FileKitComposeJvmTest.kt`

Cover:

1. Exact raw HWND forwarding to `Show`, including negative bit patterns; no parent passes a true null owner.
2. Window-free `() -> Long` conversion tests prove AWT-to-HWND success, thrown conversion, and zero-result mapping; failures become `FileKitPickerException`, not an unparented dialog.
3. XDG Open and Save receive the same correct parent string.
4. Exact X11 examples, including `1 -> "x11:1"`, `0x2a -> "x11:2a"`, and `0xffffffff -> "x11:ffffffff"`.
5. The same window-free seam proves AWT-to-XID success, thrown conversion, and zero-result mapping as `FileKitPickerException`.
6. Wayland handles receive exactly one FileKit-added prefix and are otherwise unchanged; an input of `"wayland:token"` intentionally resolves to `"wayland:wayland:token"`.
7. Injected portal availability deterministically covers Open, Multi, Directory, and Save routing in both available and unavailable states.
8. All four fallback routes reject X11/Wayland parents rather than discarding ownership.
9. Recording `PlatformFilePicker` fakes prove all eight Linux routing cases without opening AWT/Swing UI or D-Bus.
10. A host-independent class predicate accepts `Frame`/`Dialog` and rejects another `Window` class for AWT `FileDialog` ownership; the rejection path throws `FileKitPickerException`.
11. Picker and saver tests each supply an incompatible native parent and prove the shared AWT owner resolver fails before any UI opens.
12. A recording Windows show boundary proves exact `HWND?` forwarding without COM.
13. A recording XDG transport proves exact Open/Save parent strings without D-Bus.
14. Existing Windows STA and required option tests remain unchanged and green.

Any residual test that constructs a real `Window` uses a `GraphicsEnvironment.isHeadless()` assumption and records whether it ran or skipped. The existing three-OS `jvmTest` matrix must execute those tests on at least one non-headless host; if every current runner skips them, add an Xvfb-backed Linux job. The Windows Nucleus smoke test remains the required end-to-end proof of actual AWT/Tao owner behavior.

### Integration/manual verification

1. On Windows, run `:sample:nucleusApp`, open every picker/saver mode, and verify:
   - the dialog stays above the Tao window;
   - the Tao window cannot be interacted with while the modal dialog is open;
   - focus returns to the Tao window after accept and cancel;
   - the Tao event loop continues pumping while FileKit's dedicated STA thread owns the dialog, with no deadlock on either accept or cancel.
2. On Linux/X11 with a test provider capable of supplying an XID, verify portal stacking and modality.
3. On Linux/Wayland with a live `xdg_foreign` export, verify portal stacking and keep the exporter alive until completion.
4. Verify the documented failure when a native Linux parent is used with no available portal.
5. On macOS, confirm the existing application-modal picker still works and no sheet-parenting claim is made.
6. Compile and package the Nucleus sample for the current host; Windows manual behavior remains the required proof for the requested Tao integration.

## Acceptance Criteria

- FileKit exposes one canonical JVM dialog parent and no generic/native catch-all handle.
- AWT callers can express the same ownership through `FileKitDialogParent.awt`.
- Windows/Tao passes the exact nonzero HWND to every native dialog mode.
- XDG emits `x11:` as documented and FileKit canonicalizes the XID to bare lowercase hexadecimal; Wayland emits `wayland:<opaque-handle>`.
- Wayland values remain opaque; raw pointers are documented as invalid but are not guessed from string shape.
- Ownership-capable platform adapters never silently discard a supplied parent because it is incompatible or because AWT-to-native conversion fails.
- The documented exceptions are macOS `runModal()` ignoring an AWT parent and `WindowScope` deliberately replacing the caller's parent.
- Parent identities are borrowed and their lifecycle contract is documented.
- `FileKitDialogSettings` remains a data class with `parent` as its sole parent property.
- The source and binary break from 0.14.2 is documented with an AWT migration example.
- `WindowScope` continues to inject its own AWT window.
- FileKit's published modules do not depend on Nucleus.
- The Nucleus sample uses `nativeHandle` only on Windows and never passes `taoHandle`, a macOS NSView, Linux zero, or a raw Wayland surface as an HWND.
- macOS sheet parenting and Nucleus Linux parent extraction remain explicitly out of scope.
- Focused tests, JVM/module checks, repository checks, assembly, formatting, and CI pass.

## Verification

Run in this order:

```bash
./gradlew :filekit-dialogs:jvmTest \
  --tests 'io.github.vinceglb.filekit.dialogs.FileKitDialogParentTest' \
  --tests 'io.github.vinceglb.filekit.dialogs.FileKitDialogSettingsTest' \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.windows.WindowsDialogParentTest' \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.xdg.XdgDialogParentTest' \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.awt.AwtDialogOwnerTest' \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.linux.LinuxFilePickerTest' \
  --no-daemon

./gradlew :filekit-dialogs-compose:jvmTest \
  --tests 'io.github.vinceglb.filekit.dialogs.compose.FileKitComposeJvmTest' \
  --no-daemon

./gradlew :filekit-dialogs:check :filekit-dialogs-compose:check --no-daemon
./gradlew :sample:nucleusApp:compileKotlin --no-daemon
./gradlew check --no-daemon
./gradlew assemble --no-daemon
./gradlew :sample:nucleusApp:packageDistributionForCurrentOS --no-daemon

ktlint '**/*.kt' '**/*.kts' '!**/build/**' -R ktlint-compose-0.4.28-all.jar
git diff --check
```

On Windows, record the JDK, Nucleus version, backend, and results of the modal/focus smoke test.

## Out of Scope

- Production implementation during the planning/review phase.
- A generic `Long`, `Any`, Nucleus object, Tao object, or caller-defined adapter in the FileKit public interface.
- A published FileKit-Nucleus integration module.
- Extracting Nucleus internal Linux handles.
- Treating a raw `wl_surface*` as a Wayland portal parent.
- Exporting a Wayland surface on the caller's behalf.
- macOS NSView-to-NSWindow conversion or sheet presentation.
- Changing Linux portal-first picker selection.
- Changing Windows COM threading, options, cancellation, or file-result behavior.
- Changing native Windows/Kotlin, macOS/Kotlin Native, mobile, or web dialog settings.
- Version bumping, publishing, or release work.
