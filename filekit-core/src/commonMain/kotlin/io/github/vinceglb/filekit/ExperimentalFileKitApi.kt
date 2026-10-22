package io.github.vinceglb.filekit

/**
 * Marks FileKit APIs that are still being designed.
 *
 * Their signatures or behaviour may change in a future release without a deprecation cycle.
 */
@RequiresOptIn(
    message = "This FileKit API is experimental and may change without notice.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalFileKitApi
