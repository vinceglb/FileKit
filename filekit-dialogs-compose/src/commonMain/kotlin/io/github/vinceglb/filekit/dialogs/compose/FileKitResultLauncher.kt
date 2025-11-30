package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile

public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}

public class SaverResultLauncher(
    private val onLaunch: (
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
    ) -> Unit,
) {
    public fun launch(
        suggestedName: String,
        extension: String? = null,
        directory: PlatformFile? = null,
    ) {
        onLaunch(suggestedName, extension, directory)
    }
}
