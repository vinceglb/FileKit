package io.github.vinceglb.filekit.dialogs

/**
 * Windows native implementation of [FileKitOpenFileSettings].
 * Currently, there are no specific settings for opening files on Windows native.
 */
public actual class FileKitOpenFileSettings {
    public actual companion object {
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}
