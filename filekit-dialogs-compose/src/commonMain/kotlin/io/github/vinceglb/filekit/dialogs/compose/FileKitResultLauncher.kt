package io.github.vinceglb.filekit.dialogs.compose

import io.github.vinceglb.filekit.PlatformFile

/**
 * Launcher for the file picker.
 */
public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    /**
     * Launches the file picker.
     */
    public fun launch() {
        onLaunch()
    }
}

/**
 * Launcher for the file saver.
 */
public class SaverResultLauncher(
    private val onLaunch: (
        suggestedName: String,
        extension: String?,
        directory: PlatformFile?,
    ) -> Unit,
) {
    /**
     * Launches the file saver dialog.
     *
     * @param suggestedName The suggested name for the file.
     * @param extension The file extension (optional).
     * @param directory The initial directory (optional, supported on desktop).
     */
    public fun launch(
        suggestedName: String,
        extension: String? = null,
        directory: PlatformFile? = null,
    ) {
        onLaunch(suggestedName, extension, directory)
    }
}
