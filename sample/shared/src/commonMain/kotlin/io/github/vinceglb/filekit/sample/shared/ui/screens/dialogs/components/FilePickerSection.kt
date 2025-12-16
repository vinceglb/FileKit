package io.github.vinceglb.filekit.sample.shared.ui.screens.dialogs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.FeatureCard

private enum class PickerTypeOption(
    val label: String,
) {
    FileAny("File (any)"),
    FileExtensions("File (extensions)"),
    Image("Image"),
    Video("Video"),
    ImageAndVideo("Image + Video"),
}

private enum class PickerModeOption(
    val label: String,
) {
    Single("Single"),
    Multiple("Multiple"),
    SingleWithState("Single (state)"),
    MultipleWithState("Multiple (state)"),
}

@Composable
internal fun FilePickerSection(
    dialogSettings: FileKitDialogSettings,
    onDialogSettingsChange: (FileKitDialogSettings) -> Unit,
    onSingleResult: (PlatformFile?) -> Unit,
    onMultipleResult: (List<PlatformFile>?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var typeOption by remember { mutableStateOf(PickerTypeOption.FileAny) }
    var extensionsText by remember { mutableStateOf("png,jpg,pdf") }
    var modeOption by remember { mutableStateOf(PickerModeOption.Single) }
    var maxItemsText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var initialDirectory by remember { mutableStateOf<PlatformFile?>(null) }

    var stateLog by remember { mutableStateOf<String?>(null) }

    val fileKitType = remember(typeOption, extensionsText) {
        when (typeOption) {
            PickerTypeOption.FileAny -> {
                FileKitType.File()
            }

            PickerTypeOption.FileExtensions -> {
                val exts = extensionsText
                    .split(",", " ", ";")
                    .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
                    .toSet()
                    .takeIf { it.isNotEmpty() }
                FileKitType.File(exts)
            }

            PickerTypeOption.Image -> {
                FileKitType.Image
            }

            PickerTypeOption.Video -> {
                FileKitType.Video
            }

            PickerTypeOption.ImageAndVideo -> {
                FileKitType.ImageAndVideo
            }
        }
    }

    val maxItems = maxItemsText.toIntOrNull()
    val titleOrNull = titleText.takeIf { it.isNotBlank() }

    val singleLauncher = rememberFilePickerLauncher(
        type = fileKitType,
        mode = FileKitMode.Single,
        title = titleOrNull,
        directory = initialDirectory,
        dialogSettings = dialogSettings,
    ) { file ->
        stateLog = null
        onSingleResult(file)
    }

    val multipleLauncher = rememberFilePickerLauncher(
        type = fileKitType,
        mode = FileKitMode.Multiple(maxItems),
        title = titleOrNull,
        directory = initialDirectory,
        dialogSettings = dialogSettings,
    ) { files ->
        stateLog = null
        onMultipleResult(files)
    }

    val singleStateLauncher = rememberFilePickerLauncher(
        type = fileKitType,
        mode = FileKitMode.SingleWithState,
        title = titleOrNull,
        directory = initialDirectory,
        dialogSettings = dialogSettings,
    ) { state ->
        stateLog = state.toReadableLog()
        if (state is FileKitPickerState.Completed) {
            onSingleResult(state.result)
        }
    }

    val multipleStateLauncher = rememberFilePickerLauncher(
        type = fileKitType,
        mode = FileKitMode.MultipleWithState(maxItems),
        title = titleOrNull,
        directory = initialDirectory,
        dialogSettings = dialogSettings,
    ) { state ->
        stateLog = state.toReadableLog()
        if (state is FileKitPickerState.Completed) {
            onMultipleResult(state.result)
        }
    }

    FeatureCard(
        title = "File picker",
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerDropdown(
                label = "Type",
                options = PickerTypeOption.entries,
                selected = typeOption,
                onSelect = { typeOption = it },
            )

            if (typeOption == PickerTypeOption.FileExtensions) {
                OutlinedTextField(
                    value = extensionsText,
                    onValueChange = { extensionsText = it },
                    label = { Text("Extensions (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            PickerDropdown(
                label = "Mode",
                options = PickerModeOption.entries,
                selected = modeOption,
                onSelect = { modeOption = it },
            )

            if (modeOption == PickerModeOption.Multiple || modeOption == PickerModeOption.MultipleWithState) {
                OutlinedTextField(
                    value = maxItemsText,
                    onValueChange = { maxItemsText = it },
                    label = { Text("Max items (1-50, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title (desktop only, optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            DirectoryInputField(
                label = "Initial directory (desktop only)",
                directory = initialDirectory,
                onDirectoryChange = { initialDirectory = it },
            )

            DialogSettingsEditor(
                dialogSettings = dialogSettings,
                onDialogSettingsChange = onDialogSettingsChange,
            )

            Button(
                onClick = {
                    when (modeOption) {
                        PickerModeOption.Single -> singleLauncher.launch()
                        PickerModeOption.Multiple -> multipleLauncher.launch()
                        PickerModeOption.SingleWithState -> singleStateLauncher.launch()
                        PickerModeOption.MultipleWithState -> multipleStateLauncher.launch()
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Launch picker")
            }

            if (stateLog != null) {
                Text(
                    text = stateLog.orEmpty(),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> PickerDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(selected.name.replaceFirstChar { it.uppercase() })
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

private fun <T> FileKitPickerState<T>.toReadableLog(): String = when (this) {
    is FileKitPickerState.Started -> "Started ($total items)"
    is FileKitPickerState.Progress -> "Progress"
    is FileKitPickerState.Completed -> "Completed"
    FileKitPickerState.Cancelled -> "Cancelled"
}
