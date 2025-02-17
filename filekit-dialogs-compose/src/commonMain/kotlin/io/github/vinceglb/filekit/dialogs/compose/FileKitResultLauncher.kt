package io.github.vinceglb.filekit.dialogs.compose

public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}

public class SaverResultLauncher(
    private val onLaunch: (
        bytes: ByteArray?,
        baseName: String,
        extension: String,
        initialDirectory: String?,
    ) -> Unit,
) {
    public fun launch(
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunch(null, baseName, extension, initialDirectory)
    }

    @Deprecated(
        message = "Use the function without the bytes parameter. If necessary, save the bytes in the returned PlatformFile. On web targets, you can use FileKit.download() to download the bytes. More info here: https://filekit.mintlify.app/migrate-to-v0.10",
        replaceWith = ReplaceWith("launch(baseName, extension, initialDirectory)"),
    )
    public fun launch(
        bytes: ByteArray? = null,
        baseName: String = "file",
        extension: String,
        initialDirectory: String? = null,
    ) {
        onLaunch(bytes, baseName, extension, initialDirectory)
    }
}
