package io.github.vinceglb.filekit.sample.ui.screens.dialogs.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun DirectoryInputField(
    label: String,
    directory: PlatformFile?,
    onDirectoryChange: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    Text(
        text = "$label not supported on web",
        modifier = modifier,
    )
}
