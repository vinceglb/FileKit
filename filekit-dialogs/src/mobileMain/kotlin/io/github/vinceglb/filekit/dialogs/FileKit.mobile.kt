package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public enum class FileKitCameraFacing {
    System,
    Front,
    Back,
}

@OptIn(ExperimentalUuidApi::class)
public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
    cameraFacing: FileKitCameraFacing = FileKitCameraFacing.System,
    destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
): PlatformFile?

public expect suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)

public expect suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)
