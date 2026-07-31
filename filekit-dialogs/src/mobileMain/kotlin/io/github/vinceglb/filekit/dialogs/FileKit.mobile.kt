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
 * Opens the system camera picker.
 *
 * @return The captured file, or null if the user cancels or denies the runtime camera permission.
 * @throws FileKitDialogException When FileKit cannot open the camera or write the captured file.
 */
@OptIn(ExperimentalUuidApi::class)
public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
    cameraFacing: FileKitCameraFacing = FileKitCameraFacing.System,
    destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
): PlatformFile?

/**
 * Opens the system share dialog for [file].
 *
 * @throws FileKitDialogException When FileKit cannot create or present the share dialog.
 */
public expect suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)

/**
 * Opens the system share dialog for [files].
 *
 * @throws FileKitDialogException When FileKit cannot create or present the share dialog.
 */
public expect suspend fun FileKit.shareFile(
    files: List<PlatformFile>,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)
