package io.github.vinceglb.filekit.exceptions

/** The reason persisted bookmark data could not be resolved. */
public enum class BookmarkResolutionFailure {
    INVALID_DATA,
    UNSUPPORTED_VERSION,
    INCOMPATIBLE_PLATFORM,
    RESOURCE_UNAVAILABLE,
}

/** Thrown when persisted bookmark data cannot be resolved. */
public class BookmarkResolutionException : FileKitException {
    public val reason: BookmarkResolutionFailure

    public constructor(
        reason: BookmarkResolutionFailure,
        message: String,
    ) : super(message) {
        this.reason = reason
    }

    public constructor(
        reason: BookmarkResolutionFailure,
        message: String,
        cause: Throwable,
    ) : super(message, cause) {
        this.reason = reason
    }
}
