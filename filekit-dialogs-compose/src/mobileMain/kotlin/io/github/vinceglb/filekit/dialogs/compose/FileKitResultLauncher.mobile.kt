package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile

public class PhotoResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}

public class ShareResultLauncher(
    private val onLaunch: (file: PlatformFile) -> Unit,
) {
    public fun launch(file: PlatformFile) {
        onLaunch(file)
    }
}
