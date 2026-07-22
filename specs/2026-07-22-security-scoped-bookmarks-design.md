# Security-Scoped Bookmarks for macOS (Apple-native + JVM)

**Date:** 2026-07-22 (revision 2, after two spec reviews)
**Issue:** [#590](https://github.com/vinceglb/FileKit/issues/590) — BUG: MacOS App Sandbox security-scoped bookmarks (JVM + Apple-native)
**Reference implementation:** branch `vinceglb/issue-590-security-scoped-bookmarks`
(this spec aligns with and validates that branch's approach; the implementation
plan should start from it rather than from scratch)

## Problem

FileKit's `BookmarkData` does not produce real security-scoped bookmarks, so
sandboxed macOS apps (e.g. Mac App Store distribution) cannot restore file
access across launches:

1. **Apple-native** (`PlatformFile.apple.kt`): `bookmarkDataWithOptions` and
   `URLByResolvingBookmarkData` both pass `options = 0u`, and
   `bookmarkDataIsStale = null` discards staleness. On macOS this creates a
   plain (non-security-scoped) bookmark. On iOS/watchOS `0u` is correct:
   bookmarks there are implicitly security-scoped and the security-scope
   option constants do not exist.
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
- Surface staleness and a refresh recommendation to callers.
- Existing persisted bookmarks (old formats) keep resolving after upgrade,
  without perpetual-refresh loops for unsandboxed apps.
- No new dependencies, no bundled dylib, no JDK version bump. The JVM and
  Apple `PlatformFile` ABIs are preserved **manually** (see below) even though
  the classes stop being `data class`es.
- No behavior change on iOS, watchOS, Android, Windows, Linux, or web.

## Public API changes

New result type and resolve function in `nonWebMain`; the existing
`fromBookmarkData` keeps its signature and delegates:

```kotlin
public class BookmarkResolution(
    public val file: PlatformFile,
    public val isStale: Boolean,        // factual platform result (bookmarkDataIsStale)
    public val shouldRefresh: Boolean,  // FileKit's recommendation to re-create + re-persist
)

// New API.
public expect fun PlatformFile.Companion.resolveBookmarkData(
    bookmarkData: BookmarkData,
): BookmarkResolution

// Existing API — unchanged signature; delegates and drops the flags.
public expect fun PlatformFile.Companion.fromBookmarkData(
    bookmarkData: BookmarkData,
): PlatformFile
```

- `isStale` is exactly what the OS reported; never synthesized by FileKit.
- `shouldRefresh` is `isStale || legacyFormat` — set when the data came
  through a legacy decoder (pre-fix path bytes on macOS JVM, unwrapped raw
  bookmark bytes on Apple native). Docs must note that refreshing legacy data
  in a sandboxed app can fail (the legacy data carries no access grant); the
  user must then re-select the resource.
- A typed `BookmarkResolutionException` carries a reason enum
  (`INVALID_DATA`, `UNSUPPORTED_VERSION`, `INCOMPATIBLE_PLATFORM`, …) for
  diagnosable failures.
- On platforms with no staleness/legacy concept (Android, Windows, Linux,
  non-macOS JVM), both flags are `false`.

### PlatformFile ABI preservation

Carrying per-resolution access state requires state inside `PlatformFile`, so
the JVM (and Apple) `PlatformFile` change from `data class` to regular classes
that **hand-implement the previous ABI surface**: public primary-shaped
constructor, `component1()`, `copy()` with default, `equals`/`hashCode` on the
underlying `File`/`NSURL`. A `javap` baseline of the current published class
is captured in-repo, and the implementation must diff the new class against it
(the reference branch already does this in
`docs/plans/issue-590-platform-file-jvm-abi.txt` — relocate under `specs/` or
similar, since `docs/` is the Mintlify site).

## Access lifecycle

Security-scope capabilities attach to the *resolved root* URL. Derived files
(`PlatformFile(base, child)`, `list()`, `parent()`, `absoluteFile()`) are fresh
path-backed instances. macOS grants subtree access while a bookmarked
directory root is actively accessed. Design:

**Per-resolution lease, carried inside `PlatformFile`.** Each successful
resolution creates one internal access controller (retained root URL + access
refcount). The resolved `PlatformFile` holds a reference to *its* controller:

- **Descendant propagation:** internal derivation paths (`withPath`/`copy`,
  child construction, `list()` results) propagate the controller **only when
  the controller's root covers the new path** (normalized-path prefix on path
  *components*, so `/foo` does not cover `/foobar`). Parents and out-of-root
  paths get no controller.
- **Per-access binding:** `startAccessing…()` starts access on the instance's
  own controller and `stopAccessing…()` stops it on the same controller — no
  table lookup at stop time, so overlapping or later-registered roots cannot
  change which capability a balanced pair targets.
- **Idempotent, owned release:** `releaseBookmark()` releases the *instance's*
  lease exactly once (subsequent calls are no-ops). Instances that never came
  from a resolution — including fresh equal-path instances — hold no lease and
  cannot release someone else's.
- **Deferred final release:** the controller only performs the native
  stop/`CFRelease` when both its lease count *and* its active-access count
  reach zero. Releasing a bookmark while a source/sink is still open defers
  teardown until that handle closes.
- Files without a controller keep current behavior (direct `nsUrl` call on
  Apple; `true` no-op on JVM).

**Filesystem entry-point audit.** `withScopedAccess` coverage is currently
incomplete and, for handles, incorrect:

- `source()` / `sink()` stop access immediately after *constructing* the
  handle (`PlatformFile.jvmAndNative.kt`). They must return wrapping
  `RawSource`/`RawSink` implementations that acquire access on creation and
  hold it until `close()`.
- `exists()` and `createDirectories()` are not scoped at all; the
  implementation plan must audit **every** filesystem entry point
  (jvmAndNativeMain, jvmMain, appleMain, desktopMain) and scope each one.

## BookmarkData format on macOS: shared versioned envelope

**Both macOS backends** (native and JVM) wrap new bookmark bytes in the same
envelope, defined once in `nonWebMain`:

```
magic ("\0FileKitBookmark") + version (1 byte) + kind (1 byte) + payload
```

`kind` records whether the payload is a **regular** or **security-scoped**
bookmark. This matters on both backends:

- Old regular native bookmarks and new security-scoped native bookmarks are
  both opaque raw Foundation data — "try scoped resolution and fall back" is
  not a documented format discriminator, and would flag every unsandboxed
  app's bookmarks as needing refresh forever (perpetual refresh loop).
- A JVM envelope created while unsandboxed contains a regular bookmark and
  must stay distinguishable if the app later adopts App Sandbox.

Resolution logic (macOS, both backends):

1. Envelope present, known version → resolve payload with the options implied
   by `kind`; `isStale` from the OS out-parameter; `shouldRefresh = isStale`.
2. Envelope present, unknown version or kind →
   `BookmarkResolutionException(UNSUPPORTED_VERSION / INCOMPATIBLE_PLATFORM)`.
3. No envelope → **legacy**:
   - *Apple native:* treat as raw legacy bookmark data, resolve without
     security scope; `shouldRefresh = true`.
   - *JVM:* strictly decode as UTF-8 (invalid sequences →
     `BookmarkResolutionException(INVALID_DATA)`) and treat as a legacy path;
     `shouldRefresh = true`. **Known limitation:** corrupt data that happens
     to be valid UTF-8 is indistinguishable from a legacy path; strict
     decoding narrows, but cannot close, that window.

iOS/watchOS and Windows/Linux formats are unchanged (raw OS bookmark data and
bare path bytes respectively; both flags `false`).

## Part 1 — Apple-native fix

**Where:** `appleMain` with per-platform actuals (reference branch:
`AppleBookmarkConfiguration.{apple,macos,ios,watchos}.kt`).

- Internal expect/actual configuration supplies creation/resolution options:
  security-scoped on macOS, `0u` on iOS/watchOS.
- Creation on macOS wraps the result in the envelope with the correct `kind`;
  resolution follows the envelope logic above and registers the resolved URL
  with a new access controller.
- **Sandbox detection (macOS):** check the `com.apple.security.app-sandbox`
  entitlement via `SecTaskCopyValueForEntitlement`.
  - *Sandboxed creation:* security-scoped only; on error **throw** — never
    silently downgrade to data that fails after relaunch.
  - *Unsandboxed creation:* regular bookmark, `kind = Regular` — resolves
    without refresh loops.

## Part 2 — JVM bridge on macOS

**Where:** `jvmMain` (reference branch: `MacOsBookmark.jvm.kt`), gated on the
platform being macOS. Windows/Linux keep path-bytes behavior unchanged.

**CoreFoundation C API via JNA** (already a dependency) — plain C functions,
not `objc_msgSend` (typed `objc_msgSend` variants are ABI-sensitive and crash
the JVM on mistakes):

- `CFURLCreateWithFileSystemPath`, `CFURLCreateBookmarkData`
  (`kCFURLBookmarkCreationWithSecurityScope`, `1 << 11`),
  `CFURLCreateByResolvingBookmarkData`
  (`kCFURLBookmarkResolutionWithSecurityScope`, `1 << 10`, real `isStale`
  out-parameter), `CFURLStartAccessingSecurityScopedResource` /
  `CFURLStopAccessingSecurityScopedResource`.
- `SecTaskCreateFromSelf` + `SecTaskCopyValueForEntitlement` for sandbox
  detection.
- `CFRetain`/`CFRelease` confined to the access controller; standard CF
  create/copy-rule discipline elsewhere.

Behavior mirrors Part 1: sandboxed creation throws on failure; unsandboxed
creates `kind = Regular`; resolution follows the shared envelope logic and
attaches a `MacOsBookmarkAccess` controller to the returned `PlatformFile`.

## Part 3 — Documentation

Bookmark docs must cover:

- Required entitlements: `com.apple.security.app-sandbox`,
  `com.apple.security.files.user-selected.read-write`,
  `com.apple.security.files.bookmarks.app-scope`.
- The contract: check `shouldRefresh` and re-persist; refresh of legacy data
  can fail in a sandbox → prompt the user to re-select; call
  `releaseBookmark()` when done; children of a bookmarked directory work
  through FileKit operations while the resolution is alive.
- Release notes: sandboxed creation now throws where it previously
  "succeeded" with broken data (intentional).

## Testing

Automated (CI is unsandboxed; these validate mechanics that work there too):

- **Envelope:** encode/decode round-trip; unknown version throws; unknown
  kind throws; truncated header throws; empty payload throws.
- **Legacy:** unwrapped native bookmark resolves with `shouldRefresh = true`;
  JVM path bytes resolve with `shouldRefresh = true`; invalid UTF-8 throws
  `INVALID_DATA`; regular-kind envelope resolves with `shouldRefresh = false`
  (no refresh loop).
- **Lifecycle:**
  - duplicate `releaseBookmark()` on the same instance is a no-op the second
    time;
  - release through an unrelated equal-path instance does not affect the
    resolution's access;
  - two resolutions of the same path are independent leases;
  - releasing while a `source()` is open defers native release until close;
  - overlapping bookmarked roots: access started before a more-specific root
    is registered stops against the same controller it started on;
  - path-component coverage: `/foo` does not cover `/foobar`;
  - child of a bookmarked directory gets access via the propagated controller.
- **ABI:** `javap` diff of `PlatformFile` against the captured baseline —
  constructor, `component1`, `copy`, `copy$default` signatures unchanged.
- **Cross-platform:** JVM-on-Linux/Windows path-bytes unchanged; iOS simulator
  round-trip unchanged.

Manual: sandboxed, signed sample-app build on macOS (Kotlin/Native and JVM):
access across relaunch, including reading a *child* of a bookmarked directory.

## Risks

- The JNA → CoreFoundation bridge crosses into native code; CF create/copy
  mistakes leak or crash. Mitigations: plain C bindings, retain/release
  confined to the controller, everything `internal` and macOS-gated.
- Hand-rolled ABI preservation replaces compiler-generated data-class members;
  the `javap` baseline diff is the safety net and must run in CI or at least
  in the release checklist.
- Sandboxed creation now throws where it previously returned broken data —
  intentional behavior change, called out in release notes.
- Symlinked paths inside a bookmarked root may not match component-prefix
  coverage. Documented limitation.
