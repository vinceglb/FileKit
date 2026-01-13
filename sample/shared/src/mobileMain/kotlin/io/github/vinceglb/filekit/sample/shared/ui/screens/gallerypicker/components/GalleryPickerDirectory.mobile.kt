package io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun GalleryPickerDirectory(
    directory: PlatformFile?,
    onPickDirectory: (directory: PlatformFile?) -> Unit,
    modifier: Modifier,
) {
}
