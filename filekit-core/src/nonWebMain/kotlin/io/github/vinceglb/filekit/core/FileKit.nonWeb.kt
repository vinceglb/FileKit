package io.github.vinceglb.filekit.core

import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.pickDirectory(
    title: String? = null,
    initialDirectory: String? = null,
    platformSettings: FileKitPlatformSettings? = null,
): PlatformFile?
