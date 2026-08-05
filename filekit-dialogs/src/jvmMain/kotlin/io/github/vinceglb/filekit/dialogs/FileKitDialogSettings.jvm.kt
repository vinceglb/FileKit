package io.github.vinceglb.filekit.dialogs

/**
 * JVM implementation of [FileKitDialogSettings].
 *
 * @property title The title of the dialog.
 * @property parent The borrowed window identity that owns the dialog. Keep it
 * alive until the suspending picker call completes.
 * @property macOS Specific settings for macOS when running on JVM.
 */
public actual data class FileKitDialogSettings(
    public val title: String? = null,
    public val parent: FileKitDialogParent? = null,
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
