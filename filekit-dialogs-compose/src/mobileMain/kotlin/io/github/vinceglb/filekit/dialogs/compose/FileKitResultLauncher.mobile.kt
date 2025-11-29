package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.div
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public class PhotoResultLauncher(
    private val onLaunch: (
        type: FileKitCameraType,
        destinationFile: PlatformFile,
    ) -> Unit,
) {
    @OptIn(ExperimentalUuidApi::class)
    public fun launch(
        type: FileKitCameraType = FileKitCameraType.Photo,
        destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg"
    ) {
        onLaunch(type, destinationFile)
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
