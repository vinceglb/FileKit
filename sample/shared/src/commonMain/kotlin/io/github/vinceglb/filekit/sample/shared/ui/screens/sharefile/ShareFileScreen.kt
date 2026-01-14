package io.github.vinceglb.filekit.sample.shared.ui.screens.sharefile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDottedBorderCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdown
import io.github.vinceglb.filekit.sample.shared.ui.components.AppDropdownItem
import io.github.vinceglb.filekit.sample.shared.ui.components.AppField
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerResultsCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSelectionButton
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerSupportCard
import io.github.vinceglb.filekit.sample.shared.ui.components.AppPickerTopBar
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeader
import io.github.vinceglb.filekit.sample.shared.ui.components.AppScreenHeaderButtonState
import io.github.vinceglb.filekit.sample.shared.ui.icons.Check
import io.github.vinceglb.filekit.sample.shared.ui.icons.CheckCheck
import io.github.vinceglb.filekit.sample.shared.ui.icons.File
import io.github.vinceglb.filekit.sample.shared.ui.icons.LucideIcons
import io.github.vinceglb.filekit.sample.shared.ui.icons.Share
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppMaxWidth
import io.github.vinceglb.filekit.sample.shared.ui.theme.AppTheme
import io.github.vinceglb.filekit.sample.shared.util.AppUrl
import io.github.vinceglb.filekit.sample.shared.util.openUrlInBrowser
import io.github.vinceglb.filekit.sample.shared.util.plus

@Composable
internal fun ShareFileRoute(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    ShareFileScreen(
        onNavigateBack = onNavigateBack,
        onDisplayFileDetails = onDisplayFileDetails,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareFileScreen(
    onNavigateBack: () -> Unit,
    onDisplayFileDetails: (file: PlatformFile) -> Unit,
) {
    val buttonState = AppScreenHeaderButtonState.Enabled
    var pickerMode by remember { mutableStateOf(ShareMode.Multiple) }
    var selectedFiles by remember { mutableStateOf(emptyList<PlatformFile>()) }

    val shareLauncher = rememberShareFileLauncher()
    val isSupported = shareLauncher.isSupported
    val primaryButtonText = when (selectedFiles.size) {
        0 -> "Share File"
        1 -> "Share File"
        else -> "Share Files"
    }

    val singlePicker = rememberFilePickerLauncher(
        type = FileKitType.File(),
        mode = FileKitMode.Single,
    ) { file ->
        selectedFiles = file?.let(::listOf) ?: emptyList()
    }

    val multiplePicker = rememberFilePickerLauncher(
        type = FileKitType.File(),
        mode = FileKitMode.Multiple(),
    ) { files ->
        selectedFiles = files ?: emptyList()
    }

    fun pickFiles() {
        when (pickerMode) {
            ShareMode.Single -> singlePicker.launch()
            ShareMode.Multiple -> multiplePicker.launch()
        }
    }

    fun shareFiles() {
        if (!isSupported || selectedFiles.isEmpty()) {
            return
        }
        shareLauncher.launch(selectedFiles)
    }

    Scaffold(
        topBar = {
            AppPickerTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDocumentation = { AppUrl("https://filekit.mintlify.app/dialogs/share-file").openUrlInBrowser() },
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
                    icon = LucideIcons.Share,
                    title = "Share File",
                    subtitle = "Send files to other apps using the native share sheet",
                    documentationUrl = "https://filekit.mintlify.app/dialogs/share-file",
                    primaryButtonText = primaryButtonText,
                    primaryButtonEnabled = isSupported && selectedFiles.isNotEmpty(),
                    primaryButtonState = buttonState,
                    onPrimaryButtonClick = ::shareFiles,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            item {
                ShareFileSettingsCard(
                    mode = pickerMode,
                    selectedCount = selectedFiles.size,
                    isSupported = isSupported,
                    onModeChange = { newMode ->
                        pickerMode = newMode
                        if (newMode == ShareMode.Single && selectedFiles.size > 1) {
                            selectedFiles = selectedFiles.take(1)
                        }
                    },
                    onPickFiles = ::pickFiles,
                    onClearFiles = { selectedFiles = emptyList() },
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }

            if (!isSupported) {
                item {
                    AppPickerSupportCard(
                        text = "Sharing is available on Android and iOS targets.",
                        icon = LucideIcons.Share,
                        modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                    )
                }
            }

            item {
                AppPickerResultsCard(
                    files = selectedFiles,
                    emptyText = "No files selected yet",
                    emptyIcon = LucideIcons.Share,
                    onFileClick = onDisplayFileDetails,
                    modifier = Modifier.sizeIn(maxWidth = AppMaxWidth),
                )
            }
        }
    }
}

@Composable
private fun ShareFileSettingsCard(
    mode: ShareMode,
    selectedCount: Int,
    isSupported: Boolean,
    onModeChange: (ShareMode) -> Unit,
    onPickFiles: () -> Unit,
    onClearFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filesLabel = when (selectedCount) {
        0 -> null
        1 -> "1 file selected"
        else -> "$selectedCount files selected"
    }

    AppDottedBorderCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AppField(label = "Picker Mode") {
                AppDropdown(
                    value = mode,
                    onValueChange = onModeChange,
                    options = shareModeOptions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AppPickerSelectionButton(
                label = "Files",
                value = filesLabel,
                placeholder = "Select files",
                icon = LucideIcons.File,
                enabled = isSupported,
                onClick = onPickFiles,
                onClear = onClearFiles,
            )

            Text(
                text = "Tip: Share uses the platform share sheet. Files must exist on disk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

private enum class ShareMode {
    Single,
    Multiple,
}

private val shareModeOptions: List<AppDropdownItem.IconItem<ShareMode>> = listOf(
    AppDropdownItem.IconItem(
        label = "Single file",
        value = ShareMode.Single,
        icon = LucideIcons.Check,
    ),
    AppDropdownItem.IconItem(
        label = "Multiple files",
        value = ShareMode.Multiple,
        icon = LucideIcons.CheckCheck,
    ),
)

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = AndroidUiModes.UI_MODE_NIGHT_YES)
@Composable
private fun ShareFileScreenPreview() {
    AppTheme {
        ShareFileScreen(
            onNavigateBack = {},
            onDisplayFileDetails = {},
        )
    }
}
