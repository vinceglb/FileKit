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
        bytes: ByteArray?,
    ) -> Unit,
) {
    public fun launch(
        suggestedName: String,
        extension: String? = null,
        directory: PlatformFile? = null,
    ) {
        onLaunch(suggestedName, extension, directory, null)
    }

    @Deprecated(
        message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. " +
            "On web targets, you can use FileKit.download() to download the bytes. " +
            "More info here: https://filekit.mintlify.app/migrate-to-v0.10",
        replaceWith = ReplaceWith("launch(suggestedName, extension, directory)"),
    )
    public fun launch(
        bytes: ByteArray?,
        baseName: String = "file",
        extension: String? = null,
        directory: PlatformFile? = null,
    ) {
        onLaunch(baseName, extension, directory, bytes)
    }
}
