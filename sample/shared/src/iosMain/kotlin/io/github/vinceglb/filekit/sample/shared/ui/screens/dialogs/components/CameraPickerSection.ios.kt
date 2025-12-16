package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
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
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard
import io.github.vinceglb.filekit.sample.shared.ui.components.PlatformFileInfoCard

@Composable
internal actual fun CameraPickerSection(
    lastPhoto: PlatformFile?,
    onPhotoCapture: (PlatformFile?) -> Unit,
    modifier: Modifier,
) {
    var cameraFacing by remember { mutableStateOf(FileKitCameraFacing.Back) }
    var destinationPath by remember { mutableStateOf("") }

    val launcher = rememberCameraPickerLauncher { file ->
        onPhotoCapture(file)
    }

    FeatureCard(title = "Camera picker", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Type: Photo")

            EnumDropdown(
                label = "Camera facing",
                options = FileKitCameraFacing.entries,
                selected = cameraFacing,
                onSelect = { cameraFacing = it },
            )

            OutlinedTextField(
                value = destinationPath,
                onValueChange = { destinationPath = it },
                label = { Text("Destination path (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val destinationFile = destinationPath.toPlatformFileOrNull()
                    if (destinationFile != null) {
                        launcher.launch(
                            type = FileKitCameraType.Photo,
                            cameraFacing = cameraFacing,
                            destinationFile = destinationFile,
                        )
                    } else {
                        launcher.launch(
                            type = FileKitCameraType.Photo,
                            cameraFacing = cameraFacing,
                        )
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Launch camera")
            }

            if (lastPhoto != null) {
                PlatformFileInfoCard(file = lastPhoto)
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

private fun String.toPlatformFileOrNull(): PlatformFile? =
    trim()
        .takeIf(String::isNotEmpty)
        ?.let { runCatching { PlatformFile(it) }.getOrNull() }
