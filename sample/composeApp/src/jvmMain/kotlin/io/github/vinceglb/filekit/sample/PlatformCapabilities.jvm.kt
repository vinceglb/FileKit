package io.github.vinceglb.filekit.sample

actual fun platformCapabilities(): PlatformCapabilities = PlatformCapabilities(
    supportsDirectoryPicker = true,
    supportsFileSaver = true,
    supportsOpenFile = true,
    supportsCameraPicker = false,
    supportsShareFile = false,
    supportsDownload = false,
    supportsFileSystemOps = true,
    supportsImageUtils = true,
    supportsCustomPaths = true,
)
