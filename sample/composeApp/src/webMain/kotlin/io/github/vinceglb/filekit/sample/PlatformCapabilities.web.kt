package io.github.vinceglb.filekit.sample

actual fun platformCapabilities(): PlatformCapabilities = PlatformCapabilities(
    supportsDirectoryPicker = false,
    supportsFileSaver = false,
    supportsOpenFile = false,
    supportsCameraPicker = false,
    supportsShareFile = false,
    supportsDownload = true,
    supportsFileSystemOps = false,
    supportsImageUtils = false,
    supportsCustomPaths = false,
)
