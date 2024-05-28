package io.github.vinceglb.filekit.compose

public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}

public class SaverResultLauncher(
    private val onLaunch: (
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    public fun launch(
        bytes: ByteArray,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunch(bytes, baseName, extension, initialDirectory)
    }
}
