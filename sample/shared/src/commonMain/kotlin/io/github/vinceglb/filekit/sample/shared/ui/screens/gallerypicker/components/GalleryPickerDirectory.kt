package io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException

@Composable
internal expect fun GalleryPickerDirectory(
    directory: PlatformFile?,
    onError: (FileKitDialogException) -> Unit,
    onPickDirectory: (directory: PlatformFile?) -> Unit,
    modifier: Modifier = Modifier,
)
