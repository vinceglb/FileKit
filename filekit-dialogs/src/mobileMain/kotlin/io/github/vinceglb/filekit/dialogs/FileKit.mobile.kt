package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
): PlatformFile?

public expect suspend fun FileKit.shareImageFile(
    file : PlatformFile,
    fileKitShareOption: FileKitShareOption
)