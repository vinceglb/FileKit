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

/**
 * Opens a camera picker dialog.
 *
 * @return The saved file as a [PlatformFile], or `null` if the user dismisses the camera or denies camera permission.
 * @throws FileKitDialogException When a valid camera operation cannot start or complete.
 */
@OptIn(ExperimentalUuidApi::class)
public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
    cameraFacing: FileKitCameraFacing = FileKitCameraFacing.System,
    destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
): PlatformFile?

/**
 * Shares [file] with the platform share sheet.
 *
 * @throws FileKitDialogException When a valid sharing operation cannot start or complete.
 */
public expect suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)

/**
 * Shares [files] with the platform share sheet.
 *
 * @throws FileKitDialogException When a valid sharing operation cannot start or complete.
 */
public expect suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)
