package io.github.vinceglb.filekit.dialogs

import java.awt.Window

public actual data class FileKitDialogSettings(
    public val parentWindow: Window? = null,
    public val macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
) {
    public actual companion object {
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}

public class FileKitMacOSSettings(
    public val resolvesAliases: Boolean? = null,
    public val canCreateDirectories: Boolean = true,
)
