package io.github.vinceglb.filekit.compose

import io.github.vinceglb.filekit.core.IPlatformFile

public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}

public class SaverResultLauncher(
    private val onLaunch: (
        bytes: IPlatformFile?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    public fun launch(
        inputFile: IPlatformFile? = null,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunch(inputFile, baseName, extension, initialDirectory)
    }
}
