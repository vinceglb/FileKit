package io.github.vinceglb.filekit.dialogs

import java.awt.Window

/**
 * JVM implementation of [FileKitDialogSettings].
 *
 * @property title The title of the dialog.
 * @property parent The parent for the dialog, when one is available.
 * @property macOS Specific settings for macOS when running on JVM.
 */
public actual data class FileKitDialogSettings(
    public val title: String? = null,
    public val parent: FileKitDialogParent? = null,
    public val macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
) {
    /**
     * Compatibility adapter for the former AWT-only setting.
     *
     * This constructor normalizes immediately into [parent], so an instance never
     * stores competing AWT and native parents. It intentionally requires
     * [parentWindow] to keep `FileKitDialogSettings()` unambiguous.
     */
    @Deprecated(
        message = "Use parent = parentWindow?.let(FileKitDialogParent::awt) instead.",
        replaceWith = ReplaceWith(
            "FileKitDialogSettings(title = title, parent = parentWindow?.let(FileKitDialogParent::awt), macOS = macOS)",
        ),
    )
    public constructor(
        title: String? = null,
        parentWindow: Window?,
        macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
    ) : this(
        title = title,
        parent = parentWindow?.let(FileKitDialogParent::awt),
        macOS = macOS,
    )

    /**
     * Compatibility adapter for the former positional AWT constructor call.
     *
     * The parameter is intentionally named [window] so existing named
     * `parentWindow = ...` calls continue to select the adapter above.
     */
    @Deprecated(
        message = "Use parent = FileKitDialogParent.awt(window) instead.",
        replaceWith = ReplaceWith("FileKitDialogSettings(parent = FileKitDialogParent.awt(window))"),
    )
    public constructor(
        window: Window?,
        macOS: FileKitMacOSSettings = FileKitMacOSSettings(),
    ) : this(
        parent = window?.let(FileKitDialogParent::awt),
        macOS = macOS,
    )

    /**
     * The AWT window represented by [parent], if any.
     *
     * Native [FileKitDialogParent] values return `null`; callers should migrate to
     * [parent] rather than treating that result as an unparented dialog.
     */
    @Deprecated(
        message = "Use parent instead.",
        replaceWith = ReplaceWith("parent"),
    )
    public val parentWindow: Window?
        get() = parent.awtWindowOrNull()

    /**
     * Compatibility adapter for copying a former AWT-only setting.
     *
     * A canonical and a legacy parent cannot be supplied in the same call: the
     * overloads deliberately expose either `parent` or `parentWindow`, never both.
     */
    @Deprecated(
        message = "Use copy(parent = parentWindow?.let(FileKitDialogParent::awt)) instead.",
        replaceWith = ReplaceWith(
            "copy(title = title, parent = parentWindow?.let(FileKitDialogParent::awt), macOS = macOS)",
        ),
    )
    public fun copy(
        title: String? = this.title,
        parentWindow: Window?,
        macOS: FileKitMacOSSettings = this.macOS,
    ): FileKitDialogSettings = FileKitDialogSettings(
        title = title,
        parent = parentWindow?.let(FileKitDialogParent::awt),
        macOS = macOS,
    )

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
