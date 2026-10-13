package io.github.vinceglb.filekit

internal actual fun PlatformFile.resolveRelativePath(
    root: PlatformFile
): String {
    return resolveRelativePathForFileSystem(
        root = root
    )
}