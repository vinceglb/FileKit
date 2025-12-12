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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.ui.components.FeatureCard
import io.github.vinceglb.filekit.sample.ui.components.PlatformFileInfoCard
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.launch

@Composable
actual fun FileSaverSection(
    lastSavedFile: PlatformFile?,
    onFileSave: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var suggestedName by remember { mutableStateOf("sample") }
    var extension by remember { mutableStateOf("txt") }
    var content by remember { mutableStateOf("Hello from FileKit!") }
    var initialDirectory by remember { mutableStateOf<PlatformFile?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    val launcher = rememberFileSaverLauncher(
        dialogSettings = dialogSettings,
    ) { file ->
        onFileSave(file)
        if (file != null) {
            scope.launch {
                status = runCatching {
                    file.writeString(content)
                    "Wrote ${content.length} chars to ${file.name}"
                }.getOrElse { "Write failed: ${it.message}" }
            }
        } else {
            status = "Cancelled"
        }
    }

    FeatureCard(title = "File saver", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = suggestedName,
                onValueChange = { suggestedName = it },
                label = { Text("Suggested name") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = extension,
                onValueChange = { extension = it },
                label = { Text("Extension (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content to write") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
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
                onClick = {
                    val extOrNull = extension.takeIf { it.isNotBlank() }
                    launcher.launch(
                        suggestedName = suggestedName.ifBlank { "file" },
                        extension = extOrNull,
                        directory = initialDirectory,
                    )
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Launch saver")
            }

            if (status != null) {
                Text(status.orEmpty())
            }

            if (lastSavedFile != null) {
                PlatformFileInfoCard(file = lastSavedFile)
            }
        }
    }
}
