package io.github.vinceglb.filekit.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
fun DialogsScreen(
    appState: SampleAppState,
    modifier: Modifier = Modifier,
) {
    var dialogSettings by remember { mutableStateOf(FileKitDialogSettings.createDefault()) }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "Dialogs playground",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            FilePickerSection(
                dialogSettings = dialogSettings,
                onDialogSettingsChange = { dialogSettings = it },
                onSingleResult = { appState.lastPickedFile = it },
                onMultipleResult = { appState.lastPickedFiles = it },
            )
        }

        item {
            DirectoryPickerSection(
                selectedDirectory = appState.lastPickedDirectory,
                onDirectorySelect = { appState.lastPickedDirectory = it },
                dialogSettings = dialogSettings,
                onDialogSettingsChange = { dialogSettings = it },
            )
        }

        item {
            FileSaverSection(
                lastSavedFile = appState.lastSavedFile,
                onFileSave = { appState.lastSavedFile = it },
                dialogSettings = dialogSettings,
                onDialogSettingsChange = { dialogSettings = it },
            )
        }

        item {
            OpenFileSection(
                fileToOpen = appState.lastPickedFile ?: appState.lastSavedFile,
            )
        }

        item {
            CameraPickerSection(
                lastPhoto = appState.lastCameraFile,
                onPhotoCapture = { appState.lastCameraFile = it },
            )
        }

        item {
            val files = appState.lastPickedFiles ?: appState.lastPickedFile?.let { listOf(it) }.orEmpty()
            ShareFileSection(filesToShare = files)
        }
    }
}
