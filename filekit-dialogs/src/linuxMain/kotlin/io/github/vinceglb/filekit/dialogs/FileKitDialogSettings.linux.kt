package io.github.vinceglb.filekit.dialogs

/**
 * Linux native implementation of [FileKitDialogSettings].
 *
 * @property title The title of the dialog.
 */
public actual class FileKitDialogSettings(
    public val title: String? = null,
) {
    public actual companion object {
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}
