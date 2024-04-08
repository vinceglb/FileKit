package io.github.vinceglb.picker.compose

public class PickerResultLauncher(
    private val onLaunch: () -> Unit,
) {
    public fun launch() {
        onLaunch()
    }
}
