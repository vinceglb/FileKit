package io.github.vinceglb.filekit.sample

data class PlatformCapabilities(
    val supportsDirectoryPicker: Boolean,
    val supportsFileSaver: Boolean,
    val supportsOpenFile: Boolean,
    val supportsCameraPicker: Boolean,
    val supportsShareFile: Boolean,
    val supportsDownload: Boolean,
    val supportsFileSystemOps: Boolean,
    val supportsImageUtils: Boolean,
    val supportsCustomPaths: Boolean,
)

expect fun platformCapabilities(): PlatformCapabilities
