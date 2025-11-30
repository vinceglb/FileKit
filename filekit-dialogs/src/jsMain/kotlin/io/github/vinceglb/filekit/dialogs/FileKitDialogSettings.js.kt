package io.github.vinceglb.filekit.dialogs

/**
 * JS implementation of [FileKitDialogSettings].
 * Currently, there are no specific settings for JS file dialogs.
 */
public actual class FileKitDialogSettings {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitDialogSettings].
         */
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}
