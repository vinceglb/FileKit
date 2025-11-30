package io.github.vinceglb.filekit

/**
 * Represents bookmark data used for persisting access to security-scoped resources.
 *
 * @property bytes The byte array containing the bookmark data.
 */
public class BookmarkData(
    public val bytes: ByteArray,
)
