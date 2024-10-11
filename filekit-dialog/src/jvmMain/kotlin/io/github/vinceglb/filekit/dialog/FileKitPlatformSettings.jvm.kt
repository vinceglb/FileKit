package io.github.vinceglb.filekit.dialog

import java.awt.Window

public actual data class FileKitDialogSettings(
    public val parentWindow: Window? = null,
    public val macOS: FileKitMacOSSettings? = null,
)

public class FileKitMacOSSettings(
    public val resolvesAliases: Boolean? = null,
)
