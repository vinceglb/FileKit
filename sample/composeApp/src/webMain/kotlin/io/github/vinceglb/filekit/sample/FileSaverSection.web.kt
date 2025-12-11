package io.github.vinceglb.filekit.sample

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
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.download
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
    var status by remember { mutableStateOf<String?>(null) }

    FeatureCard(title = "File saver / download", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("File saver dialogs are not available on web. Use download instead.")

            OutlinedTextField(
                value = suggestedName,
                onValueChange = { suggestedName = it },
                label = { Text("File name") },
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
                label = { Text("Content to download") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Button(
                onClick = {
                    val fileName = buildString {
                        append(suggestedName.ifBlank { "file" })
                        val ext = extension.trim().takeIf(String::isNotEmpty)
                        if (ext != null) {
                            append(".")
                            append(ext)
                        }
                    }
                    scope.launch {
                        FileKit.download(content.encodeToByteArray(), fileName)
                        status = "Download triggered: $fileName"
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Download")
            }

            if (status != null) {
                Text(status.orEmpty())
            }
        }
    }
}
