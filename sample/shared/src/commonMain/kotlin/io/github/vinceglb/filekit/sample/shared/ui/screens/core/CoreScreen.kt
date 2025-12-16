package io.github.vinceglb.filekit.sample.shared.ui.screens.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.sample.shared.SampleAppState
import io.github.vinceglb.filekit.sample.shared.ui.screens.core.components.DirectoriesSection
import io.github.vinceglb.filekit.sample.shared.ui.screens.core.components.FileSystemOperationsSection
import io.github.vinceglb.filekit.sample.shared.ui.screens.core.components.ImageUtilsSection
import io.github.vinceglb.filekit.sample.shared.ui.screens.core.components.WebDownloadSection

@Composable
internal fun CoreScreen(
    appState: SampleAppState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Core playground",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            DirectoriesSection()
        }

        item {
            FileSystemOperationsSection(
                selectedFile = appState.lastPickedFile,
                onFileUpdate = { appState.lastPickedFile = it },
            )
        }

        item {
            ImageUtilsSection(
                selectedFile = appState.lastPickedFile ?: appState.lastCameraFile,
            )
        }

        item {
            WebDownloadSection()
        }
    }
}
