package io.github.vinceglb.filekit.sample.ui.screens.dialogs.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard

@Composable
actual fun DirectoryPickerSection(
    selectedDirectory: PlatformFile?,
    onDirectorySelect: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier,
) {
    FeatureCard(title = "Directory picker", modifier = modifier) {
        Text("Directory picker is not supported on web.")
    }
}
