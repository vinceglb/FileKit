package io.github.vinceglb.filekit.core

import java.awt.Window

public actual data class FileKitPlatformSettings(
    public val parentWindow: Window? = null,
    public val macOS: FileKitMacOSSettings? = null,
)

public class FileKitMacOSSettings(
    public val resolvesAliases: Boolean? = null,
)
