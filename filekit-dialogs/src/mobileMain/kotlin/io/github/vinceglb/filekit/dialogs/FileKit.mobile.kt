package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
public expect suspend fun FileKit.openCameraPicker(
    type: FileKitCameraType = FileKitCameraType.Photo,
    destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
): PlatformFile?

public expect suspend fun FileKit.shareFile(
    file: PlatformFile,
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
)
