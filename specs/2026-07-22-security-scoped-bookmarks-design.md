# Security-Scoped Bookmarks for macOS (Apple-native + JVM)

**Date:** 2026-07-22 (revised after spec review)
**Issue:** [#590](https://github.com/vinceglb/FileKit/issues/590) — BUG: MacOS App Sandbox security-scoped bookmarks (JVM + Apple-native)

## Problem

FileKit's `BookmarkData` does not produce real security-scoped bookmarks, so
sandboxed macOS apps (e.g. Mac App Store distribution) cannot restore file
access across launches:

1. **Apple-native** (`PlatformFile.apple.kt`): `bookmarkDataWithOptions` and
   `URLByResolvingBookmarkData` both pass `options = 0u`, and
   `bookmarkDataIsStale = null` discards staleness. On macOS this creates a
   plain (non-security-scoped) bookmark. On iOS/watchOS `0u` is correct:
   bookmarks there are implicitly security-scoped and the
   `NSURLBookmarkCreationWithSecurityScope` /
   `NSURLBookmarkResolutionWithSecurityScope` constants do not exist.
2. **JVM** (`PlatformFile.jvm.kt`): `bookmarkData()` stores raw path bytes and
   `fromBookmarkData()` decodes them back into a path. This grants no access in
   a sandboxed macOS JVM app. `startAccessingSecurityScopedResource()` /
   `stopAccessingSecurityScopedResource()` are stubs.

Additionally, the access lifecycle around bookmarks has pre-existing gaps that
make directory bookmarks unusable in a sandbox even once the bookmark data
itself is fixed (see "Access lifecycle" below).

## Goals

- Sandboxed macOS apps (Kotlin/Native and JVM/Compose Desktop) can persist and
  restore file access across launches — including access to the **children**
  of a bookmarked directory.
- Never report bookmark-creation success in a sandboxed process unless the
  data is actually security-scoped.
- Surface "this bookmark should be re-created and re-persisted" to callers.
- Existing persisted bookmarks (old formats) keep resolving after upgrade.
- No new dependencies, no bundled dylib, no JDK version bump, no
  binary-incompatible change to the `PlatformFile` data classes.
- No behavior change on iOS, watchOS, Android, Windows, Linux, or web.

## Public API changes

New rich result type and resolve function in `nonWebMain`; the existing
`fromBookmarkData` keeps its signature and delegates:

```kotlin
public class ResolvedBookmark(
    public val file: PlatformFile,
    public val isStale: Boolean,
)

// New API — surfaces staleness.
public expect fun PlatformFile.Companion.resolveBookmark(
    bookmarkData: BookmarkData,
): ResolvedBookmark

// Existing API — unchanged signature; delegates to resolveBookmark and
// drops the staleness flag.
public expect fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile
```

**`isStale` semantics:** "the bookmark data should be re-created via
`bookmarkData()` and re-persisted." It is set when:

- the OS reports the bookmark as stale (`bookmarkDataIsStale`), or
- the data was resolved through a **legacy-format** decoder (pre-fix path
  bytes on macOS JVM, or a non-security-scoped bookmark on macOS native).

This single flag deliberately folds "system-stale" and "legacy format"
together because the caller's action is identical in both cases. On platforms
with neither concept (Android, Windows, Linux, non-macOS JVM),
`resolveBookmark` returns `isStale = false`.

`PlatformFile` (JVM and Apple `data class`es) gains **no new properties** —
constructor, `copy()`, and `componentN()` signatures are unchanged. All
capability state lives in an internal side-table (below).

## Access lifecycle (shared design, both targets)

Security-scope capabilities attach to the *resolved root* (the URL that came
out of bookmark resolution). Derived files — `PlatformFile(base, child)`,
`list()` results, `parent()`, `absoluteFile()` — are fresh path-backed
instances and carry no capability. Access to them requires the **root's**
capability to be active. macOS grants subtree access while a bookmarked
directory root is actively accessed, so the design is:

**An internal, refcounted capability table** (per target):

- Key: normalized absolute path of the resolved bookmark root.
- Value: the retained root capability (resolved `NSURL` on Apple native;
  retained `CFURL` pointer on JVM) + an access refcount.
- Resolving the same path twice increments a resolve-count on the existing
  entry instead of overwriting it — no orphaned retained pointers.
- `startAccessingSecurityScopedResource()`: find the entry for the file's
  path **or its nearest ancestor** (longest-prefix match on normalized
  paths). If found, start access on the root capability (balanced,
  refcounted — the native stop is only called when the count returns to
  zero). If not found, fall back to current behavior (direct `nsUrl` call on
  Apple; `true` no-op on JVM).
- `stopAccessingSecurityScopedResource()`: decrement; balanced with
  successful starts only.
- `releaseBookmark()`: decrement the entry's resolve-count; on zero, stop any
  remaining access, release the retained capability, remove the entry.
  Idempotent — releasing an already-released or never-bookmarked file is a
  no-op.

This gives child files access "for free" through the existing
`withScopedAccess` call sites, works for equal-by-path `PlatformFile`
instances, and requires no change to the public `PlatformFile` classes.

**Filesystem entry-point audit.** `withScopedAccess` coverage is currently
incomplete and, for handles, incorrect:

- `source()` / `sink()` stop access immediately after *constructing* the
  handle, before it is read/written
  (`PlatformFile.jvmAndNative.kt`). They must return wrapping
  `RawSource`/`RawSink` implementations that hold access until `close()`.
- `exists()` and `createDirectories()` are not scoped at all; the
  implementation plan must audit **every** filesystem entry point
  (jvmAndNativeMain, jvmMain, appleMain, desktopMain) and scope each one.

## Part 1 — Apple-native fix

**Where:** `appleMain` with per-platform actuals.

- Replace the hardcoded `0u` with internal expect properties:

  ```kotlin
  // appleMain
  internal expect val bookmarkCreationOptions: NSURLBookmarkCreationOptions
  internal expect val bookmarkResolutionOptions: NSURLBookmarkResolutionOptions
  ```

  - `macosMain` actuals: `NSURLBookmarkCreationWithSecurityScope` and
    `NSURLBookmarkResolutionWithSecurityScope`.
  - `iosMain` and `watchosMain` actuals: `0u`.

- Resolution passes a real `bookmarkDataIsStale` pointer, registers the
  resolved URL in the capability table, and returns `ResolvedBookmark`.

- **Sandbox detection (macOS):** check the
  `com.apple.security.app-sandbox` entitlement via
  `SecTaskCopyValueForEntitlement(SecTaskCreateFromSelf(...), ...)`.
  - *Creation, sandboxed:* security-scoped creation only; on error, **throw
    `FileKitException`** — never silently downgrade to unscoped data that
    would fail after relaunch.
  - *Creation, unsandboxed:* create a regular (non-scoped) bookmark with
    `0u` — security scope is meaningless there and this matches current
    behavior.
  - *Resolution:* try with the security-scope option first; on failure retry
    with `0u` (legacy bookmark from an older FileKit) and set
    `isStale = true` so callers migrate.

## Part 2 — JVM bridge on macOS

**Where:** `jvmMain`, gated on `os.name` containing "mac". Windows/Linux keep
the current path-bytes behavior unchanged.

### Native bridge: CoreFoundation C API via JNA

New internal object (e.g. `MacSecurityScopedBookmarks`) using JNA — already a
`filekit-core` JVM dependency (`libs.jna.platform`) — binding **plain C
functions**, not `objc_msgSend` (typed `objc_msgSend` variants are
ABI-sensitive and crash the JVM on mistakes; the CF API removes selectors and
calling-convention risk entirely):

- `CFURLCreateWithFileSystemPath` — build the CFURL.
- `CFURLCreateBookmarkData` with
  `kCFURLBookmarkCreationWithSecurityScope` (`1 << 11`).
- `CFURLCreateByResolvingBookmarkData` with
  `kCFURLBookmarkResolutionWithSecurityScope` (`1 << 10`) and a real
  `isStale` out-parameter.
- `CFURLStartAccessingSecurityScopedResource` /
  `CFURLStopAccessingSecurityScopedResource`.
- `SecTaskCreateFromSelf` + `SecTaskCopyValueForEntitlement` (Security
  framework) for sandbox detection.
- `CFRetain` / `CFRelease` with explicit ownership: resolved CFURLs are
  retained, stored in the capability table, and released only via
  `releaseBookmark()`. Standard CF create/copy-rule discipline for
  everything else.

### BookmarkData format on macOS JVM: versioned envelope

New bookmark bytes are wrapped in a small FileKit envelope:
`"FKBK"` magic + 1 version byte + payload (the raw CF bookmark data).
Resolution logic:

1. Envelope magic present, known version → unwrap, resolve via CF API;
   `isStale` from the OS out-parameter.
2. Envelope magic present, **unknown** version → throw `FileKitException`
   (data from a future FileKit; corrupting it silently is worse than failing).
3. No envelope → legacy path bytes: decode as path string, return with
   `isStale = true` so callers re-create a real bookmark.

This distinguishes legacy, current, corrupt, and future formats without
guess-based "try one decoder, then the other" resolution. The envelope is
macOS-JVM-only: Windows/Linux keep bare path bytes (their current format, not
legacy — `isStale = false`), and Apple-native bytes stay raw OS bookmark data
(no ambiguity exists there; legacy detection uses the resolution fallback in
Part 1).

### Behavior on macOS JVM

- `bookmarkData()`: sandboxed → security-scoped CF bookmark in envelope, or
  **throw** on failure (no silent path-bytes downgrade); unsandboxed →
  regular CF bookmark in envelope (falling back to path bytes only if the CF
  call fails, which cannot lose access rights outside the sandbox).
- `resolveBookmark()` / `fromBookmarkData()`: per the envelope rules above;
  successful CF resolutions register the retained CFURL in the capability
  table.
- `startAccessing` / `stopAccessing` / `releaseBookmark()`: capability-table
  semantics from "Access lifecycle" above.

## Part 3 — Documentation

Update the bookmark documentation to call out the entitlements required for
sandboxed macOS apps:

- `com.apple.security.app-sandbox`
- `com.apple.security.files.user-selected.read-write` (initial pick access)
- `com.apple.security.files.bookmarks.app-scope` (persistence across launches)

Document the contract: check `ResolvedBookmark.isStale` and re-persist a fresh
bookmark when set; call `releaseBookmark()` when done with a resolved file;
children of a bookmarked directory are accessible while working through
FileKit's file operations.

## Testing

Automated (CI is not sandboxed; these validate the mechanics, which also work
in non-sandboxed processes):

- `macosArm64`: create → resolve round-trip (`isStale = false`); legacy
  (options `0u`) bookmark resolves via fallback with `isStale = true`;
  child-file access resolves the root capability via ancestor lookup;
  double-resolve + release is balanced (no crash, no premature release);
  `releaseBookmark()` is idempotent.
- JVM-on-macOS: same round-trip through the CF bridge; envelope encode/decode;
  legacy path-bytes resolve with `isStale = true`; unknown envelope version
  throws; registry refcounting and idempotent release; `source()`/`sink()`
  hold access until close.
- JVM-on-Linux/Windows: path-bytes behavior unchanged, `isStale = false`.
- iOS simulator: existing bookmark round-trip still passes (options stay `0u`).

Manual: verify true sandbox behavior (access across app relaunch, including
reading a *child* of a bookmarked directory) with the sample app's Bookmarks
screen in a sandboxed, signed build on macOS — both Kotlin/Native and JVM.

## Risks

- The JNA → CoreFoundation bridge still crosses into native code; CF
  create/copy-rule mistakes leak or crash. Mitigations: plain C bindings
  (no `objc_msgSend`), explicit retain/release confined to the capability
  table, everything `internal` and macOS-gated.
- Sandboxed creation now throws where it previously "succeeded" with broken
  data. This is intentional (silent downgrade reproduces the reported bug),
  but is a behavior change worth calling out in release notes.
- The longest-prefix ancestor lookup assumes normalized absolute paths;
  symlinked paths inside a bookmarked root may miss the capability. Documented
  limitation; ops still work when the caller holds the root access open.
