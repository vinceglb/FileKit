package io.github.vinceglb.filekit.dialogs

/**
 * iOS implementation of [FileKitOpenFileSettings].
 * Currently, there are no specific settings for opening files on iOS.
 */
public actual class FileKitOpenFileSettings {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenFileSettings].
         */
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}
