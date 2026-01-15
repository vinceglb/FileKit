package io.github.vinceglb.filekit.dialogs

/**
 * Android implementation of [FileKitOpenFileSettings].
 *
 * @property authority The content authority string used for creating a [android.net.Uri].
 * Defaults to "{applicationId}.FileKitFileProvider" when null.
 */
public actual class FileKitOpenFileSettings(
    public val authority: String? = null,
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitOpenFileSettings].
         */
        public actual fun createDefault(): FileKitOpenFileSettings = FileKitOpenFileSettings()
    }
}
