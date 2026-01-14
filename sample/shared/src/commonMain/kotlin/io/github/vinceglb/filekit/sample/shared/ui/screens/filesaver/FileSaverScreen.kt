package io.github.vinceglb.filekit.sample.shared.ui.screens.filesaver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
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
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppOutlinedTextField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSelectionButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSupportCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.Folder
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.Share
import io.github.vinceglb.filekit.sample.shared.ui.screens.directorypicker.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.ui.theme.geistMonoFontFamily
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun FileSaverRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    FileSaverScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileSaverScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    var buttonState by remember { mutableStateOf(AppScreenHeaderButtonState.Enabled) }
    var suggestedName by remember { mutableStateOf("document") }
    var extension by remember { mutableStateOf("pdf") }
    var saveDirectory by remember { mutableStateOf<PlatformFile?>(null) }
    var savedFiles by remember { mutableStateOf(emptyList<PlatformFile>()) }

    val fileSaverLauncher = rememberFileSaverLauncher { file ->
        buttonState = AppScreenHeaderButtonState.Enabled
        if (file != null) {
            savedFiles = listOf(file) + savedFiles
        }
    }
    val directoryPickerLauncher = rememberDirectoryPickerLauncher(
        directory = saveDirectory,
    ) { directory ->
        if (directory != null) {
            saveDirectory = directory
        }
    }
    val isSupported = fileSaverLauncher.isSupported
    val primaryButtonText = if (isSupported) "Save File" else "File Saver Unavailable"

    fun openFileSaver() {
        if (!isSupported) {
            return
        }
        val resolvedName = suggestedName.trim().ifBlank { "document" }
        val resolvedExtension = extension.trim().removePrefix(".").ifBlank { null }
        buttonState = AppScreenHeaderButtonState.Loading
        fileSaverLauncher.launch(
            suggestedName = resolvedName,
            extension = resolvedExtension,
            directory = saveDirectory,
        )
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/file-saver").openUrlInBrowser() },
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
                    title = "File Saver",
                    subtitle = "Pick a destination and write bytes to create the file",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/file-saver",
                    primaryButtonText = primaryButtonText,
                    primaryButtonEnabled = isSupported,
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::openFileSaver,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                FileSaverSettingsCard(
                    suggestedName = suggestedName,
                    extension = extension,
                    saveDirectoryName = saveDirectory?.name,
                    isSupported = isSupported,
                    onSuggestedNameChange = { suggestedName = it },
                    onExtensionChange = { newValue ->
                        val trimmed = newValue.trim()
                        extension = if (trimmed.startsWith(".")) trimmed.drop(1) else trimmed
                    },
                    onPickSaveDirectory = directoryPickerLauncher::launch,
                    onClearSaveDirectory = { saveDirectory = null },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            if (!isSupported) {
                item {
                    AppPickerSupportCard(
                        text = "File saver is unavailable on web. Use FileKit.download() for browser downloads.",
                        icon = LucideIcons.Share,
                        modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                    )
                }
            }

            item {
                AppPickerResultsCard(
                    files = savedFiles,
                    emptyText = "No save locations selected yet",
                    emptyIcon = LucideIcons.File,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun FileSaverSettingsCard(
    suggestedName: String,
    extension: String,
    saveDirectoryName: String?,
    isSupported: Boolean,
    onSuggestedNameChange: (String) -> Unit,
    onExtensionChange: (String) -> Unit,
    onPickSaveDirectory: () -> Unit,
    onClearSaveDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monoFontFamily = geistMonoFontFamily()

    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppField(
                    label = "File Name",
                    modifier = Modifier.weight(1f),
                ) {
                    AppOutlinedTextField(
                        value = suggestedName,
                        onValueChange = onSuggestedNameChange,
                        placeholder = {
                            Text(
                                text = "document",
                                color = MaterialTheme.colorScheme.outline,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                AppField(
                    label = "Extension",
                    modifier = Modifier.weight(1f),
                ) {
                    AppOutlinedTextField(
                        value = extension,
                        onValueChange = onExtensionChange,
                        placeholder = {
                            Text(
                                text = "pdf",
                                color = MaterialTheme.colorScheme.outline,
                                fontFamily = monoFontFamily,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = monoFontFamily,
                    )
                }
            }

            AppPickerSelectionButton(
                label = "Save In",
                value = saveDirectoryName,
                placeholder = "System default",
                icon = LucideIcons.Folder,
                enabled = isSupported,
                onClick = onPickSaveDirectory,
                onClear = onClearSaveDirectory,
            )

            Text(
                text = "Tip: File saver only returns a destination path. Write bytes to create the file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun FileSaverScreenPreview() {
    AppTheme {
        FileSaverScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
