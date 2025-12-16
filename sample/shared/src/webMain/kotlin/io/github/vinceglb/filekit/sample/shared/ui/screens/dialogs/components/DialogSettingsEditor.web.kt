package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal actual fun DialogSettingsEditor(
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier,
) {
    Text(
        text = "No dialog settings on web",
        modifier = modifier,
    )
}
