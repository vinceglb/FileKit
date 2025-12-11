package io.github.vinceglb.filekit.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
expect fun DirectoryPickerSection(
    selectedDirectory: PlatformFile?,
    onDirectorySelect: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun FileSaverSection(
    lastSavedFile: PlatformFile?,
    onFileSave: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun OpenFileSection(
    fileToOpen: PlatformFile?,
    modifier: Modifier = Modifier,
)

@Composable
expect fun CameraPickerSection(
    lastPhoto: PlatformFile?,
    onPhotoCapture: (PlatformFile?) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
expect fun ShareFileSection(
    filesToShare: List<PlatformFile>,
    modifier: Modifier = Modifier,
)
