package io.github.vinceglb.filekit

/**
 * Derives this source entry's relative filesystem path below [root].
 *
 * Uses the absolute directory prefix without canonicalizing links or normalizing
 * references to `.` and `..`. Any required security-scoped access is managed by the caller.
 *
 * @param root The source directory containing this entry.
 * @return The child path below [root], excluding the root's name.
 * @throws IllegalArgumentException If the absolute source path does not start with
 * the absolute directory prefix of [root].
 */
internal actual fun PlatformFile.resolveRelativePath(
    root: PlatformFile,
): String = resolveRelativePathForFileSystem(
    root = root,
)
