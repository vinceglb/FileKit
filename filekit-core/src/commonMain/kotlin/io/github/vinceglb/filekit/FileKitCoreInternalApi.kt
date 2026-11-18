package io.github.vinceglb.filekit

/**
 * Marks APIs used for coordination between FileKit modules.
 *
 * These APIs are public only because they cross module boundaries and may change without notice.
 */
@RequiresOptIn(
    message = "This API is internal to FileKit modules and may change without notice.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
public annotation class FileKitCoreInternalApi