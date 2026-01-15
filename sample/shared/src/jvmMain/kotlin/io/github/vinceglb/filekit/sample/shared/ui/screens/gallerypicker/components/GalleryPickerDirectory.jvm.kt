package io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSelectionButton
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons

@androidx.compose.runtime.Composable
internal actual fun GalleryPickerDirectory(
    directory: io.github.vinceglb.filekit.PlatformFile?,
    onPickDirectory: (directory: io.github.vinceglb.filekit.PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    val directoryPicker = rememberDirectoryPickerLauncher { pickedDirectory ->
        pickedDirectory?.let { onPickDirectory(pickedDirectory) }
    }

    AppPickerSelectionButton(
        label = "Directory",
        value = directory?.name,
        placeholder = "Default",
        icon = LucideIcons.Folder,
        onClick = directoryPicker::launch,
        onClear = { onPickDirectory(null) },
        modifier = modifier.fillMaxWidth(),
    )
}
