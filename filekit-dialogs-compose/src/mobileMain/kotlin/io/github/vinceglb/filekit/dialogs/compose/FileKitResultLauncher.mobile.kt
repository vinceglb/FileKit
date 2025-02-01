package io.github.vinceglb.filekit.dialogs.compose

public class PhotoResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}