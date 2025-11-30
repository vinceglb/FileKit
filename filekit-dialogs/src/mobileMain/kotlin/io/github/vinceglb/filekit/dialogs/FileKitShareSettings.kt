package io.github.vinceglb.filekit.dialogs

/**
 * Settings for sharing files.
 */
public expect class FileKitShareSettings {
    public companion object {
        /**
         * Creates a default instance of [FileKitShareSettings].
         */
        public fun createDefault(): FileKitShareSettings
    }
}
