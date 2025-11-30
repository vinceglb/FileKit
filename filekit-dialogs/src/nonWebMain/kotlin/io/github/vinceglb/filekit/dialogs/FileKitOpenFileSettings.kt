package io.github.vinceglb.filekit.dialogs

/**
 * Settings for opening a file with the default application.
 */
public expect class FileKitOpenFileSettings {
    public companion object {
        /**
         * Creates a default [FileKitOpenFileSettings].
         */
        public fun createDefault(): FileKitOpenFileSettings
    }
}
