package io.github.vinceglb.filekit.dialog.compose

public class PhotoResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}