package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

@Composable
internal actual fun ShareFileSection(
    filesToShare: List<PlatformFile>,
    modifier: Modifier,
) {
    var metaTitle by remember { mutableStateOf(FileKitShareSettings.createDefault().metaTitle) }

    val launcher = key(metaTitle) {
        rememberShareFileLauncher(
            shareSettings = FileKitShareSettings(metaTitle = metaTitle),
        )
    }

    FeatureCard(title = "Share file", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (filesToShare.isEmpty()) {
                Text("Pick a file first.")
                return@Column
            }

            OutlinedTextField(
                value = metaTitle,
                onValueChange = { metaTitle = it },
                label = { Text("Share sheet title") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { launcher.launch(filesToShare) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Share (${filesToShare.size})")
            }
        }
    }
}
