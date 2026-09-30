package io.github.vinceglb.filekit

public expect object FileKit

/**
 * Returns the directory for persistent application files.
 *
 * On Android, this corresponds to `Context.filesDir`.
 * On Apple, this corresponds to `NSApplicationSupportDirectory`.
 * On JVM, this corresponds to a platform-specific application data directory.
 * On web targets this is the root of the browser's origin private file system (OPFS).
 */
public expect suspend fun FileKit.filesDirectory(): PlatformFile
