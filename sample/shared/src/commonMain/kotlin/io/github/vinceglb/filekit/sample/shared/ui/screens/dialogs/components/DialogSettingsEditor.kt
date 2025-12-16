package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal expect fun DialogSettingsEditor(
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier = Modifier,
)
