package io.github.vinceglb.filekit.sample.shared.ui.screens.gallerypicker.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedButton
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.X

@androidx.compose.runtime.Composable
internal actual fun GalleryPickerDirectory(
    directory: io.github.vinceglb.filekit.PlatformFile?,
    onPickDirectory: (directory: io.github.vinceglb.filekit.PlatformFile?) -> Unit,
    modifier: androidx.compose.ui.Modifier,
) {
    val directoryPicker = rememberDirectoryPickerLauncher { pickedDirectory ->
        pickedDirectory?.let { onPickDirectory(pickedDirectory) }
    }

    AppField(
        label = "Directory",
        modifier = modifier,
    ) {
        AppOutlinedButton(
            onClick = directoryPicker::launch,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentPadding = PaddingValues(start = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = LucideIcons.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(directory?.name ?: "Default")
                Spacer(modifier = Modifier.weight(1f))
                AnimatedVisibility(visible = directory != null) {
                    IconButton(onClick = { onPickDirectory(null) }) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
