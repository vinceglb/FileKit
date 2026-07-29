---
status: accepted
---

# Use Versioned Native Bookmarks for macOS Persistence

FileKit will store new macOS bookmark data in a versioned FileKit envelope containing a native macOS bookmark. Bookmark creation will automatically use a security scope when the running application adopts App Sandbox and a regular native bookmark otherwise. Unwrapped data remains supported through platform-specific legacy resolution, and successfully resolved legacy data will be recommended for refresh.

`PlatformFile` remains the capability-carrying abstraction: a file restored from a security-scoped bookmark retains that capability, descendants within a bookmarked directory inherit it, and FileKit balances scoped access around actual resource use. This preserves the existing API instead of introducing a separate access-session type.

## Considered Options

- Keeping JVM paths and unscoped native bookmarks was rejected because it cannot persist access for sandboxed macOS applications.
- Always requesting a security-scoped bookmark was rejected because security scope is specific to App Sandbox and can fail for resources that remain valid regular bookmarks.
- Rejecting or eagerly migrating all legacy data was rejected because old references may still be usable and FileKit cannot recreate access that macOS has already revoked.
- Introducing an explicit scoped-file type was rejected because it would disrupt existing `PlatformFile` usage and weaken backward compatibility.

## Consequences

The bookmark envelope becomes a persistent compatibility contract and must be decoded defensively. Existing JVM-facing `PlatformFile` constructor, copy/component methods, equality, and binary signatures must remain compatible even if the implementation stops being a Kotlin data class internally. A fix is only complete after signed, sandboxed, cross-launch verification on both JVM and Kotlin/Native macOS.
