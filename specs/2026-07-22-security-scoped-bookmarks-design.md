# Security-Scoped Bookmarks for macOS (Apple-native + JVM)

**Date:** 2026-07-22
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

## Goals

- Sandboxed macOS apps (Kotlin/Native and JVM/Compose Desktop) can persist and
  restore file access across launches.
- Surface bookmark staleness so callers can refresh persisted bookmarks.
- Existing persisted bookmarks (old formats) keep resolving after upgrade.
- No new dependencies, no bundled dylib, no JDK version bump.
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

When `isStale` is true, the file resolved successfully but the caller should
call `bookmarkData()` again and re-persist the result.

On platforms with no staleness concept (Android, Windows, Linux, non-macOS
JVM), `resolveBookmark` returns `isStale = false`.

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

- Resolution passes a real `bookmarkDataIsStale` pointer and returns
  `ResolvedBookmark`.

- **Fallbacks** (macOS only, where the options differ from `0u`):
  - *Creation:* if security-scoped creation returns an error (e.g. edge cases
    in non-sandboxed processes), retry with `0u`.
  - *Resolution:* if security-scoped resolution fails (bookmark persisted by an
    older FileKit version without security scope), retry with `0u`. This keeps
    existing persisted bookmarks working.

- Access lifecycle is already correct on Apple native: `PlatformFile` wraps the
  resolved `NSURL` and file operations go through `withScopedAccess`, which
  calls `startAccessingSecurityScopedResource()` /
  `stopAccessingSecurityScopedResource()` per operation. Once the bookmark is
  security-scoped, these calls become effective. `releaseBookmark()` remains a
  no-op on Apple native.

## Part 2 — JVM bridge on macOS

**Where:** `jvmMain`, gated on `os.name` containing "mac". Windows/Linux keep
the current path-bytes behavior unchanged.

### Objective-C bridge

New internal object (e.g. `MacSecurityScopedBookmarks`) using **JNA direct
mapping to the Objective-C runtime** — JNA is already a `filekit-core` JVM
dependency (`libs.jna.platform`). It binds `objc_getClass`, `sel_registerName`,
`objc_msgSend` (and the autorelease-pool push/pop functions) from
`libobjc.dylib` / Foundation to call:

- `NSURL.fileURLWithPath:` — build a URL from the file path.
- `bookmarkDataWithOptions:includingResourceValuesForKeys:relativeToURL:error:`
  with `NSURLBookmarkCreationWithSecurityScope` (`1 << 11`).
- `URLByResolvingBookmarkData:options:relativeToURL:bookmarkDataIsStale:error:`
  with `NSURLBookmarkResolutionWithSecurityScope` (`1 << 10`) and a real
  `isStale` out-pointer.
- `startAccessingSecurityScopedResource` / `stopAccessingSecurityScopedResource`
  on the resolved URL.

Memory/threading rules for the bridge:

- Every bridge call runs inside an explicit autorelease pool
  (`objc_autoreleasePoolPush` / `objc_autoreleasePoolPop`).
- Resolved `NSURL`s that outlive the pool are explicitly `retain`ed, and
  `release`d in `releaseBookmark()`.
- All Objective-C interaction stays `internal`; no public API exposes pointers.

### Behavior on macOS JVM

- `bookmarkData()`: create real security-scoped bookmark bytes. On error, fall
  back to path bytes (matches the non-sandboxed status quo).
- `resolveBookmark()` / `fromBookmarkData()`: **try NSURL bookmark resolution
  first; if that fails, decode the bytes as a path string.** Old JVM bookmarks
  are path bytes, so they fail NSURL resolution and hit the fallback — 
  backward compatibility without a format/version header.
- **Access lifecycle registry:** an internal `ConcurrentHashMap` mapping
  resolved absolute path → retained `NSURL` pointer, populated on resolution.
  - `startAccessingSecurityScopedResource()` / `stop...()` on JVM stop being
    stubs: they look up the registry and delegate to the native calls when an
    entry exists; otherwise they keep returning `true` / no-op.
  - Since JVM file operations already route through `withScopedAccess`,
    per-operation scoped access works without touching call sites.
  - `releaseBookmark()`: stops access if active, releases the retained
    `NSURL`, removes the registry entry.

## Part 3 — Documentation

Update the bookmark documentation to call out the entitlements required for
sandboxed macOS apps:

- `com.apple.security.app-sandbox`
- `com.apple.security.files.user-selected.read-write` (initial pick access)
- `com.apple.security.files.bookmarks.app-scope` (persistence across launches)

Note the staleness contract: check `ResolvedBookmark.isStale` and re-persist a
fresh bookmark when set.

## Testing

Automated (CI is not sandboxed; these validate the mechanics, which also work
in non-sandboxed processes):

- `macosArm64` test: create → resolve round-trip returns the same file and
  `isStale = false`; resolution of a legacy (options `0u`) bookmark succeeds
  via the fallback.
- JVM-on-macOS test: same round-trip through the JNA bridge; legacy path-bytes
  `BookmarkData` still resolves via the fallback; `releaseBookmark()` clears
  the registry entry.
- JVM-on-Linux/Windows tests: path-bytes behavior unchanged.
- iOS simulator test: existing bookmark round-trip still passes (options stay
  `0u`).

Manual: verify true sandbox behavior (access across app relaunch) with the
sample app's Bookmarks screen in a sandboxed, signed build on macOS — both the
Kotlin/Native and JVM sample targets.

## Risks

- The JNA → `objc_msgSend` bridge is the main risk: incorrect selector
  signatures or retain/release mistakes crash the JVM rather than throwing.
  Mitigations: keep the bridge `internal` and macOS-gated, wrap calls with
  error-pointer checks, fall back to path-bytes behavior on any bridge failure.
- Security-scoped creation in non-sandboxed processes: handled by the `0u`
  creation fallback on both targets.
