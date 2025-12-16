package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

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
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitOpenFileSettings
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard
import io.github.vinceglb.filekit.sample.shared.ui.components.PlatformFileInfoCard

@Composable
internal actual fun OpenFileSection(
    fileToOpen: PlatformFile?,
    modifier: Modifier,
) {
    var authority by remember { mutableStateOf(FileKitOpenFileSettings.createDefault().authority) }

    FeatureCard(title = "Open file with default app", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (fileToOpen == null) {
                Text("Pick or save a file first.")
                return@Column
            }

            PlatformFileInfoCard(file = fileToOpen)

            OutlinedTextField(
                value = authority,
                onValueChange = { authority = it },
                label = { Text("FileProvider authority") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    FileKit.openFileWithDefaultApplication(
                        file = fileToOpen,
                        openFileSettings = FileKitOpenFileSettings(authority = authority),
                    )
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Open")
            }
        }
    }
}
