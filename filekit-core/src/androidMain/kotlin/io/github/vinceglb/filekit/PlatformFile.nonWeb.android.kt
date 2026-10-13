package io.github.vinceglb.filekit

internal actual fun PlatformFile.resolveRelativePath(
    root: PlatformFile
): String {
    return when(this.androidFile) {
        is AndroidFile.FileWrapper -> {
            resolveRelativePathForFileSystem(
                root = root
            )
        }
        is AndroidFile.UriWrapper -> {

        }
    }
}