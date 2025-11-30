package io.github.vinceglb.filekit.dialogs

/**
 * macOS implementation of [FileKitOpenFileSettings].
 * Currently, there are no specific settings for opening files on macOS.
 */
public actual class FileKitOpenFileSettings {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenFileSettings].
         */
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}
