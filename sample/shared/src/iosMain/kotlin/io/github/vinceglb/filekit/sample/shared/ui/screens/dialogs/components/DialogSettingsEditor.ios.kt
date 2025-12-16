package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal actual fun DialogSettingsEditor(
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier,
) {
    var canCreateDirectories by remember(dialogSettings) { mutableStateOf(dialogSettings.canCreateDirectories) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Can create directories")
        Switch(
            checked = canCreateDirectories,
            onCheckedChange = {
                canCreateDirectories = it
                onDialogSettingsChange(FileKitDialogSettings(canCreateDirectories = it))
            },
        )
    }
}
