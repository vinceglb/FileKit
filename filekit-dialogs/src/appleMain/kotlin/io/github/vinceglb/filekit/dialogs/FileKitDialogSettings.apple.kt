package io.github.vinceglb.filekit.dialogs

/**
 * Apple (iOS/macOS) implementation of [FileKitDialogSettings].
 *
 * @property canCreateDirectories Whether the user can create directories in the save panel. Defaults to true.
 */
public actual class FileKitDialogSettings(
    public val canCreateDirectories: Boolean = true,
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitDialogSettings].
         */
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}
