package io.github.vinceglb.filekit.sample.shared.ui.screens.filepicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitPickerState
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdown
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdownItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSelectionButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.Check
import io.github.vinceglb.filekit.sample.shared.ui.icons.CheckCheck
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Home
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.ui.theme.geistMonoFontFamily
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun FilePickerRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    FilePickerScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilePickerScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var pickerMode by remember { mutableStateOf(Modes.Single) }
    var pickerMaxItems: Int? by remember { mutableStateOf(null) }
    var customExtensions by remember { mutableStateOf("") }
    var startDirectory by remember { mutableStateOf<PlatformFile?>(null) }
    var files by remember { mutableStateOf(emptyList<PlatformFile>()) }

    val dialogSettingsState = rememberFilePickerDialogSettingsState()

    val startDirectoryLauncher = rememberDirectoryPickerLauncher(
        directory = startDirectory,
    ) { directory ->
        if (directory != null) {
            startDirectory = directory
        }
    }

    val resolvedType = resolveFilePickerType(customExtensions)

    val singlePicker = rememberFilePickerLauncher(
        type = resolvedType,
        mode = FileKitMode.Single,
        directory = startDirectory,
        dialogSettings = dialogSettingsState.build(),
    ) { selectedFile ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = selectedFile?.let(::listOf) ?: emptyList()
    }

    val multiplePicker = rememberFilePickerLauncher(
        type = resolvedType,
        mode = FileKitMode.Multiple(maxItems = pickerMaxItems),
        directory = startDirectory,
        dialogSettings = dialogSettingsState.build(),
    ) { selectedFiles ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = selectedFiles ?: emptyList()
    }

    val singleWithStatePicker = rememberFilePickerLauncher(
        type = resolvedType,
        mode = FileKitMode.SingleWithState,
        directory = startDirectory,
        dialogSettings = dialogSettingsState.build(),
    ) { state ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = when (state) {
            FileKitPickerState.Cancelled -> emptyList()
            is FileKitPickerState.Completed<PlatformFile> -> listOf(state.result)
            is FileKitPickerState.Progress<PlatformFile> -> listOf(state.processed)
            is FileKitPickerState.Started -> emptyList()
        }
    }

    val multipleWithStatePicker = rememberFilePickerLauncher(
        type = resolvedType,
        mode = FileKitMode.MultipleWithState(maxItems = pickerMaxItems),
        directory = startDirectory,
        dialogSettings = dialogSettingsState.build(),
    ) { state ->
        buttonState = AppScreenHeaderButtonState.Enabled
        files = when (state) {
            FileKitPickerState.Cancelled -> emptyList()
            is FileKitPickerState.Completed<List<PlatformFile>> -> state.result
            is FileKitPickerState.Progress<List<PlatformFile>> -> state.processed
            is FileKitPickerState.Started -> emptyList()
        }
    }

    val primaryButtonText = when (pickerMode) {
        Modes.Single,
        Modes.SingleWithState,
        -> "Pick File"

        Modes.Multiple,
        Modes.MultipleWithState,
        -> "Pick Files"
    }

    fun openFilePicker() {
        buttonState = AppScreenHeaderButtonState.Loading
        when (pickerMode) {
            Modes.Single -> singlePicker.launch()
            Modes.Multiple -> multiplePicker.launch()
            Modes.SingleWithState -> singleWithStatePicker.launch()
            Modes.MultipleWithState -> multipleWithStatePicker.launch()
        }
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/file-picker").openUrlInBrowser() },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                AppScreenHeader(
                    icon = LucideIcons.File,
                    title = "File Picker",
                    subtitle = "Browse and select files using the native picker",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/file-picker",
                    primaryButtonText = primaryButtonText,
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openFilePicker,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                FilePickerSettingsCard(
                    pickerMode = pickerMode,
                    pickerMaxItems = pickerMaxItems,
                    customExtensions = customExtensions,
                    startDirectoryName = startDirectory?.name,
                    isStartDirectorySupported = startDirectoryLauncher.isSupported,
                    dialogSettingsState = dialogSettingsState,
                    onPickerModeChange = { pickerMode = it },
                    onMaxItemsChange = { pickerMaxItems = it },
                    onExtensionsChange = { customExtensions = it },
                    onPickStartDirectory = startDirectoryLauncher::launch,
                    onClearStartDirectory = { startDirectory = null },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                AppPickerResultsCard(
                    files = files,
                    emptyText = "No files selected yet",
                    emptyIcon = LucideIcons.File,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun FilePickerSettingsCard(
    pickerMode: Modes,
    pickerMaxItems: Int?,
    customExtensions: String,
    startDirectoryName: String?,
    isStartDirectorySupported: Boolean,
    dialogSettingsState: FilePickerDialogSettingsState,
    onPickerModeChange: (Modes) -> Unit,
    onMaxItemsChange: (Int?) -> Unit,
    onExtensionsChange: (String) -> Unit,
    onPickStartDirectory: () -> Unit,
    onClearStartDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppField(
                    label = "Mode",
                    modifier = Modifier.weight(1f),
                ) {
                    AppDropdown(
                        value = pickerMode,
                        onValueChange = onPickerModeChange,
                        options = modeOptions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val isMaxItemsEnabled = pickerMode == Modes.Multiple || pickerMode == Modes.MultipleWithState
                AnimatedVisibility(visible = isMaxItemsEnabled) {
                    AppField(
                        label = "Max Items",
                        modifier = Modifier.weight(1f),
                    ) {
                        AppOutlinedTextField(
                            value = pickerMaxItems?.toString() ?: "",
                            onValueChange = { newValue ->
                                val number = newValue.toIntOrNull()
                                onMaxItemsChange(
                                    when {
                                        number != null && number > 50 -> 50
                                        else -> number
                                    },
                                )
                            },
                            placeholder = {
                                Text(
                                    text = "-",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontFamily = geistMonoFontFamily(),
                                )
                            },
                            modifier = Modifier.width(100.dp),
                            fontFamily = geistMonoFontFamily(),
                        )
                    }
                }
            }

            AppField(label = "Extensions (optional)") {
                AppOutlinedTextField(
                    value = customExtensions,
                    onValueChange = onExtensionsChange,
                    placeholder = {
                        Text(
                            text = "pdf, png",
                            color = MaterialTheme.colorScheme.outline,
                            fontFamily = geistMonoFontFamily(),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = geistMonoFontFamily(),
                )
            }

            AppPickerSelectionButton(
                label = "Start In",
                value = startDirectoryName,
                placeholder = "System default",
                icon = LucideIcons.Home,
                enabled = isStartDirectorySupported,
                onClick = onPickStartDirectory,
                onClear = onClearStartDirectory,
            )

            FilePickerDialogSettingsContent(state = dialogSettingsState)
        }
    }
}

private enum class Modes {
    Single,
    Multiple,
    SingleWithState,
    MultipleWithState,
}

private val modeOptions: List<AppDropdownItem.IconItem<Modes>> = listOf(
    AppDropdownItem.IconItem(
        label = "Single",
        value = Modes.Single,
        icon = LucideIcons.Check,
    ),
    AppDropdownItem.IconItem(
        label = "Single with state",
        value = Modes.SingleWithState,
        icon = LucideIcons.Check,
    ),
    AppDropdownItem.IconItem(
        label = "Multiple",
        value = Modes.Multiple,
        icon = LucideIcons.CheckCheck,
    ),
    AppDropdownItem.IconItem(
        label = "Multiple with state",
        value = Modes.MultipleWithState,
        icon = LucideIcons.CheckCheck,
    ),
)

private fun resolveFilePickerType(customExtensions: String): FileKitType {
    val extensions = customExtensions
        .split(Regex("[,\\s;]+"))
        .map { it.trim().removePrefix(".") }
        .filter { it.isNotEmpty() }
        .distinct()

    return if (extensions.isEmpty()) {
        FileKitType.File()
    } else {
        FileKitType.File(extensions)
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun FilePickerScreenPreview() {
    AppTheme {
        FilePickerScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
