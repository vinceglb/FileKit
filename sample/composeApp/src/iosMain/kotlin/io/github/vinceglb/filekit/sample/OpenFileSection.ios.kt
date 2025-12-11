package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileWithDefaultApplication

@Composable
actual fun OpenFileSection(
    fileToOpen: PlatformFile?,
    modifier: Modifier,
) {
    FeatureCard(title = "Open file with default app", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (fileToOpen == null) {
                Text("Pick or save a file first.")
                return@Column
            }

            PlatformFileInfoCard(file = fileToOpen)

            Button(
                onClick = { FileKit.openFileWithDefaultApplication(fileToOpen) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Open")
            }
        }
    }
}
