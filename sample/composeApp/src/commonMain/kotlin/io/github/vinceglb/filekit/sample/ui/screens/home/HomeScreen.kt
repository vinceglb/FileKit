package io.github.vinceglb.filekit.sample.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.sample.SampleAppState
import io.github.vinceglb.filekit.sample.getPlatform
import io.github.vinceglb.filekit.sample.platformCapabilities
import io.github.vinceglb.filekit.sample.ui.components.PlatformFileInfoCard

@Composable
fun HomeScreen(
    appState: SampleAppState,
    modifier: Modifier = Modifier,
) {
    val platformName = remember { getPlatform().name }
    val capabilities = remember { platformCapabilities() }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Platform: $platformName",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            Text(
                text = "Capabilities",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        val capabilityRows = listOf(
            "File picker" to true,
            "Directory picker" to capabilities.supportsDirectoryPicker,
            "File saver" to capabilities.supportsFileSaver,
            "Open file with default app" to capabilities.supportsOpenFile,
            "Camera picker" to capabilities.supportsCameraPicker,
            "Share file" to capabilities.supportsShareFile,
            "Web download" to capabilities.supportsDownload,
            "File system ops" to capabilities.supportsFileSystemOps,
            "Image utils" to capabilities.supportsImageUtils,
        )

        items(capabilityRows.size) { index ->
            val (label, supported) = capabilityRows[index]
            CapabilityRow(label = label, supported = supported)
        }

        item {
            Text(
                text = "Last selection",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        item {
            val lastFile = appState.lastPickedFile
            if (lastFile != null) {
                PlatformFileInfoCard(file = lastFile)
            } else {
                Text("Pick a file to see its details.")
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    label: String,
    supported: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Text(if (supported) "Supported" else "Not supported")
    }
}
