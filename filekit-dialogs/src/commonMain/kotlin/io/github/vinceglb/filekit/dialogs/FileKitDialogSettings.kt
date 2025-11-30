package io.github.vinceglb.filekit.dialogs

/**
 * Settings for the file picker dialog.
 */
public expect class FileKitDialogSettings {
    public companion object {
        /**
         * Creates a default [FileKitDialogSettings].
         */
        public fun createDefault(): FileKitDialogSettings
    }
}
