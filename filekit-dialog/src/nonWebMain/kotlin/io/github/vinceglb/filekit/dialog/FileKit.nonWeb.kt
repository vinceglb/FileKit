package io.github.vinceglb.filekit.dialog

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.pickDirectory(
    title: String? = null,
    initialDirectory: String? = null,            // TODO change to PlatformFile?
    platformSettings: FileKitDialogSettings? = null,
): PlatformFile?
