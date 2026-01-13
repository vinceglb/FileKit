package io.github.vinceglb.filekit.dialogs

import java.awt.Window

/**
 * JVM implementation of [FileKitDialogSettings].
 *
 * @property title The title of the dialog.
 * @property parentWindow The parent window for the dialog.
 * @property macOS Specific settings for macOS when running on JVM.
 */
public actual data class FileKitDialogSettings(
    public val title: String? = null,
    public val parentWindow: Window? = null,
    public val macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
) {
    public actual companion object {
        /**
         * Creates a default instance of [FileKitDialogSettings].
         */
        public actual fun createDefault(): FileKitDialogSettings = FileKitDialogSettings()
    }
}

/**
 * Settings specific to macOS file dialogs on JVM.
 *
 * @property resolvesAliases Whether aliases should be resolved.
 * @property canCreateDirectories Whether the user can create directories in the save panel.
 */
public class FileKitMacOSSettings(
    public val resolvesAliases: Boolean? = null,
    public val canCreateDirectories: Boolean = true,
)
