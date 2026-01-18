package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.div
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public class PhotoResultLauncher(
    private val onLaunch: (
        type: FileKitCameraType,
        cameraFacing: FileKitCameraFacing,
        destinationFile: PlatformFile,
    ) -> Unit,
) {
    @OptIn(ExperimentalUuidApi::class)
    public fun launch(
        type: FileKitCameraType = FileKitCameraType.Photo,
        cameraFacing: FileKitCameraFacing = FileKitCameraFacing.System,
        destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
    ) {
        onLaunch(type, cameraFacing, destinationFile)
    }
}

public class ShareResultLauncher(
    private val onLaunch: (files: List<PlatformFile>) -> Unit,
) {
    public fun launch(file: PlatformFile) {
        onLaunch(listOf(file))
    }

    public fun launch(files: List<PlatformFile>) {
        onLaunch(files)
    }
}
