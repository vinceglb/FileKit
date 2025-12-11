package io.github.vinceglb.filekit.sample

actual fun platformCapabilities(): PlatformCapabilities = PlatformCapabilities(
    supportsDirectoryPicker = true,
    supportsFileSaver = true,
    supportsOpenFile = true,
    supportsCameraPicker = true,
    supportsShareFile = true,
    supportsDownload = false,
    supportsFileSystemOps = true,
    supportsImageUtils = true,
    supportsCustomPaths = true,
)
