# File Access

FileKit provides cross-platform references to user-selected files while respecting each platform's access model.

## Language

**Bookmark Data**:
An opaque, platform-specific persistent reference to a file or directory. It may also preserve an access grant when the platform supports that capability, and must not be assumed to be portable between platforms.
_Avoid_: Saved path, serialized file

**Security-Scoped Bookmark**:
A macOS bookmark that preserves sandbox access to a user-selected file or directory across application launches.
_Avoid_: Apple bookmark, iOS security-scoped bookmark

**Stale Bookmark**:
A bookmark that the platform reports as still resolvable but in need of replacement with newly created bookmark data. Staleness is not a FileKit version marker and is not the same as an invalid bookmark.
_Avoid_: Invalid bookmark, missing file

**Legacy Bookmark Data**:
Bookmark data created by an older FileKit behavior that does not preserve the access guarantees of the current platform implementation. FileKit attempts to resolve it permissively and only requires user reselection after an actual access or refresh failure.
_Avoid_: Stale bookmark

**Bookmark Refresh**:
Advisory replacement of successfully resolved bookmark data because the platform reports it as stale or FileKit recognizes it as legacy. Refresh preserves or upgrades a reference; it does not mean the old bookmark is unusable and does not recover access that the operating system has already revoked.
_Avoid_: Permission recovery, bookmark resolution

**Bookmark Resolution**:
Interpretation of stored bookmark data to recover a `PlatformFile` and information about the stored representation. Successful resolution identifies the resource but does not guarantee that every later file operation will succeed.
_Avoid_: File access, permission grant

**Access Capability**:
Platform-granted authority carried by a `PlatformFile` to access a user-selected resource. A directory's capability also covers files derived within that directory.
_Avoid_: Path permission, permanent access

**Scoped Access**:
A bounded period during which an application activates the access represented by a security-scoped resource.
_Avoid_: Bookmark lifetime, permanent access
