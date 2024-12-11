package io.github.vinceglb.filekit.dialog

public actual class FileKitDialogSettings(
    public val canCreateDirectories: Boolean = true,
) {
    public actual companion object {
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}
