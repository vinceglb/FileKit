package io.github.vinceglb.picker.compose

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
        fileName: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    public fun launch(
        bytes: ByteArray,
        fileName: String,
        initialDirectory: String? = null,
    ) {
        onLaunch(bytes, fileName, initialDirectory)
    }
}
