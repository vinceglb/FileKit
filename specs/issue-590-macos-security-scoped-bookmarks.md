# Issue 590: macOS Security-Scoped Bookmarks

## Objective

Make bookmark persistence work across application launches for sandboxed macOS applications on JVM and Kotlin/Native while preserving existing bookmark bytes and the published `PlatformFile` API.

## Public API

Add a successful resolution value:

```kotlin
public class BookmarkResolution(
    public val file: PlatformFile,
    public val isStale: Boolean,
    public val shouldRefresh: Boolean,
)
```

Add `PlatformFile.resolveBookmarkData(BookmarkData)` and its `ByteArray` convenience overload. Keep both existing `fromBookmarkData` overloads and delegate them to the new resolver while discarding the additional metadata.

Keep resolution exception-based. Add `BookmarkResolutionException : FileKitException` with a reason that distinguishes invalid data, unsupported envelope versions, incompatible platforms, and unavailable resources. Existing callers that catch `FileKitException` remain compatible.

`isStale` reflects the platform resolver's stale flag. `shouldRefresh` is advisory and is true for a stale bookmark or any successfully resolved legacy macOS representation. Refresh can fail and does not recover permission already revoked by macOS.

## Bookmark Format

Define a compact binary envelope with:

- A FileKit bookmark magic value.
- A format version.
- A payload kind identifying a regular or security-scoped macOS native bookmark.
- The opaque native bookmark payload.

Reject recognized envelopes with unknown versions or incompatible payload kinds explicitly. When the magic value is absent, use the existing runtime-specific legacy decoder:

- JVM macOS interprets the legacy bytes as the previously stored path.
- Kotlin/Native macOS resolves the previous unwrapped Foundation bookmark.
- Other platforms retain their current bookmark formats and behavior.

Do not promise that bookmark data is portable between operating systems or applications.

## macOS Bookmark Creation and Resolution

Introduce a small platform abstraction shared by the macOS implementations:

1. Detect the running process's `com.apple.security.app-sandbox` entitlement.
2. In a sandboxed application, create and resolve bookmarks with the macOS security-scope options. Do not silently fall back to an unscoped bookmark if scoped creation fails.
3. In an unsandboxed application, create and resolve regular native bookmarks.
4. Capture the native stale flag during resolution.
5. Preserve the resolved native URL as the access capability owned by the resulting `PlatformFile`.

For Kotlin/Native, move the macOS-specific option selection out of the shared Apple behavior so iOS keeps its supported bookmark semantics. The macOS implementation uses Foundation APIs directly.

For JVM macOS, add a focused JNA bridge to the stable CoreFoundation and Security C APIs. FileKit Core already depends on JNA Platform, so this must not add a bundled native library or JNI component. Guard all native loading behind a macOS runtime check.

## Capability Lifetime

Represent a restored macOS capability as a shared internal object containing its scope root and retained native URL. Derived files inherit it only when their normalized location is the root or a descendant of it.

Balance every successful start with a stop. Support nested access safely and release retained native objects when no longer reachable.

Audit all filesystem entry points. In particular:

- Apply scoped access to currently uncovered operations such as existence checks and directory creation.
- Keep access active until returned `RawSource` and `RawSink` instances are closed rather than stopping immediately after constructing them.
- Preserve the capability through child resolution, applicable parent/absolute transformations, and JVM `copy()`.
- Do not propagate a directory capability to a location outside its scope.

`withScopedAccess` remains the explicit bridge for third-party libraries that operate on paths or handles outside FileKit's own operations.

## JVM Compatibility

If the JVM actual class can no longer remain a Kotlin data class, reproduce its published surface manually:

- `PlatformFile(File)` constructor.
- `component1()`.
- `copy(File)` and its default-argument JVM helper.
- Existing equality, hash-code, and string behavior.

Capture the current JVM signatures before changing the class and compare the built artifact against that baseline. Add regression coverage for copying the same file, copying within a bookmarked directory, and copying outside the capability root.

## Legacy Behavior

Legacy resolution is permissive:

1. Attempt the old decoder when no envelope is present.
2. Return the file with `shouldRefresh = true` when resolution succeeds.
3. Allow normal file access to proceed.
4. Let the application regenerate and persist bookmark data.
5. Require user reselection only after an actual access or refresh failure.

Old sandboxed bookmarks may identify an external resource without carrying a persistent access grant. FileKit cannot upgrade those bytes after macOS has revoked access; this limitation must be documented explicitly.

## Documentation

Update the bookmark guide to:

- Separate iOS, Kotlin/Native macOS, JVM macOS, and other JVM behavior.
- Define `isStale`, `shouldRefresh`, legacy data, and refresh failure.
- Show the new resolver and an opportunistic persistence refresh.
- Explain that `fromBookmarkData` remains the simple compatibility API but does not expose refresh metadata.
- Document App Sandbox, user-selected read/write access, and app-scoped bookmark entitlements for persistent external access.
- Explain balanced scoped access and the `withScopedAccess` escape hatch.
- Include the unavoidable reselection path for legacy bookmarks that never contained a persistent grant.

## Verification

Add automated coverage for:

- Envelope round trips and malformed input.
- Unknown versions and incompatible payload kinds.
- Both JVM-path and Kotlin/Native Foundation legacy formats.
- Current, stale, and legacy resolution metadata.
- Automatic sandbox-mode selection through pure selectors fed by the platform entitlement readers.
- Balanced and nested capability access.
- Capability propagation within a directory and rejection outside it.
- `RawSource` and `RawSink` lifetime behavior.
- JVM public and binary compatibility.

Run the repository gates required by `AGENTS.md`, including `./gradlew :filekit-core:check` and `./gradlew assemble`.

Before closing the issue, manually verify signed sandboxed applications on JVM macOS and Kotlin/Native macOS:

1. Pick an external directory.
2. Persist its bookmark and write a file.
3. Terminate the process completely.
4. Relaunch and resolve the bookmark.
5. Write again without presenting a picker.
6. Confirm access is relinquished after use.
7. Repeat with accessible and inaccessible legacy bookmark data.

Record the operating system version, entitlements, packaging method, and results in the pull request description.

## Out of Scope

- Configurable read-only versus read/write bookmark modes.
- Changing Android, iOS, Windows, or Linux bookmark formats.
- Guaranteeing cross-platform or cross-application bookmark portability.
- Silently replacing bookmark bytes in application-owned storage.
