package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public class PhotoResultLauncher(
    private val onLaunch: (destinationFile: PlatformFile) -> Unit,
) {
    @OptIn(ExperimentalUuidApi::class)
    public fun launch(
        destinationFile: PlatformFile = FileKit.cacheDir / "${Uuid.random()}.jpg",
    ) {
        onLaunch(destinationFile)
    }
}

public class ShareResultLauncher(
    private val onLaunch: (file: PlatformFile) -> Unit,
) {
    public fun launch(file: PlatformFile) {
        onLaunch(file)
    }
}
