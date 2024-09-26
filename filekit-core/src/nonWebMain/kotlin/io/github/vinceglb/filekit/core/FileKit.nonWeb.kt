package io.github.vinceglb.filekit.core

public expect suspend fun FileKit.pickDirectory(
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): PlatformFile?
