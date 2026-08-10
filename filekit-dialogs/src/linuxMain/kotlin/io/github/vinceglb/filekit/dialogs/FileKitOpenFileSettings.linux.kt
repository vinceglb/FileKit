package io.github.vinceglb.filekit.dialogs

/**
 * Linux native implementation of [FileKitOpenFileSettings].
 * Currently, there are no specific settings for opening files on Linux native.
 */
public actual class FileKitOpenFileSettings {
    public actual companion object {
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}
