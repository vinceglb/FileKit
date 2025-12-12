package io.github.vinceglb.filekit.sample.ui.screens.dialogs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard
import io.github.vinceglb.filekit.sample.ui.components.PlatformFileInfoCard

@Composable
actual fun DirectoryPickerSection(
    selectedDirectory: PlatformFile?,
    onDirectorySelect: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier,
) {
    var titleText by remember { mutableStateOf("") }
    var initialDirectory by remember { mutableStateOf<PlatformFile?>(null) }

    val launcher = rememberDirectoryPickerLauncher(
        title = titleText.takeIf { it.isNotBlank() },
        directory = initialDirectory,
        dialogSettings = dialogSettings,
    ) { picked ->
        onDirectorySelect(picked)
    }

    FeatureCard(title = "Directory picker", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title (desktop only, optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            DirectoryInputField(
                label = "Initial directory (desktop only)",
                directory = initialDirectory,
                onDirectoryChange = { initialDirectory = it },
            )

            DialogSettingsEditor(
                dialogSettings = dialogSettings,
                onDialogSettingsChange = onDialogSettingsChange,
            )

            Button(
                onClick = { launcher.launch() },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Launch picker")
            }

            if (selectedDirectory != null) {
                PlatformFileInfoCard(file = selectedDirectory)
            }
        }
    }
}
