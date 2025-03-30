package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension

public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
): PlatformFile?

public expect suspend fun FileKit.shareImageFile(
    file: PlatformFile,
    fileKitShareSettings: FileKitShareSettings
)

public fun PlatformFile.checkIsSupportImageFile(): Boolean {
    return this.extension == "jpg" || this.extension == "png" || this.extension == "jpeg"
}