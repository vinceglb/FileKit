package io.github.vinceglb.filekit

/**
 * Represents bookmark data used for persisting access to security-scoped resources.
 *
 * @property bytes The byte array containing the bookmark data.
 */
public class BookmarkData(
    public val bytes: ByteArray,
)

/**
 * The result of resolving persisted [BookmarkData].
 *
 * @property file The file identified by the bookmark.
 * @property isStale Whether the platform reported that the bookmark data is stale.
 * @property shouldRefresh Whether FileKit recommends replacing the persisted bookmark data.
 */
public class BookmarkResolution(
    public val file: PlatformFile,
    public val isStale: Boolean,
    public val shouldRefresh: Boolean,
)
