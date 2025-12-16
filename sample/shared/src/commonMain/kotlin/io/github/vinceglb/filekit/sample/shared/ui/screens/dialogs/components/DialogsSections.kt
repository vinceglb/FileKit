package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings

@Composable
internal expect fun DirectoryPickerSection(
    selectedDirectory: PlatformFile?,
    onDirectorySelect: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
internal expect fun FileSaverSection(
    lastSavedFile: PlatformFile?,
    onFileSave: (PlatformFile?) -> Unit,
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
internal expect fun OpenFileSection(
    fileToOpen: PlatformFile?,
    modifier: Modifier = Modifier,
)

@Composable
internal expect fun CameraPickerSection(
    lastPhoto: PlatformFile?,
    onPhotoCapture: (PlatformFile?) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
internal expect fun ShareFileSection(
    filesToShare: List<PlatformFile>,
    modifier: Modifier = Modifier,
)
