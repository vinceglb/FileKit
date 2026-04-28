package io.github.vinceglb.filekit.dialogs

/**
 * Apple (iOS/macOS) implementation of [FileKitDialogSettings].
 *
 * @property title The title of the dialog.
 * @property canCreateDirectories Whether the user can create directories in the save panel. Defaults to true.
 * @property assetRepresentationMode The preferred iOS photo/video picker asset representation mode.
 */
public actual class FileKitDialogSettings(
    public val title: String? = null,
    public val canCreateDirectories: Boolean = true,
    public val assetRepresentationMode: FileKitAssetRepresentationMode = FileKitAssetRepresentationMode.Automatic,
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitDialogSettings].
         */
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}
